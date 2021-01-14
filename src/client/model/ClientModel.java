package worth.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import worth.client.model.rmi.RMICallbackNotify;
import worth.client.model.rmi.RMICallbackNotifyImpl;
import worth.data.*;
import worth.protocol.CommunicationProtocol;
import worth.protocol.RequestMessage;
import worth.protocol.ResponseMessage;
import worth.exceptions.*;
import worth.protocol.UDPMessage;
import worth.server.rmi.RMICallbackService;
import worth.server.rmi.RMIRegistrationService;
import worth.utils.MyObjectMapper;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Model (logica) del client secondo il pattern MVC
 */
public class ClientModel {
    private static final int ALLOCATION_SIZE = 512*512; // spazio di allocazione del buffer
    private boolean isLogged;                           // l'utente è online?
    private String username;                            // per tenere traccia dello username dell'utente
    private final SocketChannel socket;                 // socket per instaurazione connessione
    private final ObjectMapper mapper;                  // mapper per serializzazione/deserializzazione
    private Map<String, UserStatus> userStatus;         // lista degli stati degli utenti
    private RMICallbackNotify callbackNotify;           // gestione callback
    private Map<String, String> projectChatAddresses;   // indirizzi multicast dei progetti
    private ExecutorService threadPool;                 // threadpool per servizio di lettura chat
    private MulticastSocket multicastSocket;            // socket per chat multicast
    private LocalDateTime lastListProjectsCall;         // l'ultima volta che ho chiamato listProjects

    // predispone la connessione del client con il server
    public ClientModel() throws IOException {
        // apre connessione TCP con il server
        this.socket = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(
                CommunicationProtocol.SERVER_IP_ADDRESS,
                CommunicationProtocol.SERVER_PORT
        );
        this.socket.connect(address); // bloccante per il client

        this.mapper = new MyObjectMapper();

        this.userStatus = null; // non è ancora il momento di inizializzarlo
        this.callbackNotify = null; // non è ancora il momento di inizializzarlo
        this.projectChatAddresses = new HashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.multicastSocket = new MulticastSocket(CommunicationProtocol.UDP_CHAT_PORT);
        this.multicastSocket.setSoTimeout(1000); // receive bloccante per 1 secondo
        this.isLogged = false;
        this.username = "";
    }

    public void closeConnection() {
        if (!this.isLogged) return;
        try {
            this.unregisterForCallback();
            this.socket.close();

            // shutdown threads lettori delle chat
            shutdownThreadPool();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Operazione registrazione via RMI
     */
    public void register(String username, String password)
            throws RemoteException, NotBoundException, CharactersNotAllowedException,
            UsernameNotAvailableException, PasswordTooShortException {
        // realizza connessione RMI per il servizio di registrazione
        Registry registry = LocateRegistry.getRegistry(CommunicationProtocol.REGISTRY_PORT);
        RMIRegistrationService regService =
                (RMIRegistrationService) registry.lookup(CommunicationProtocol.REGISTRATION_SERVICE_NAME);
        // call al servizio RMI
        regService.register(username, password);
    }
    /**
     * Operazioni TCP
     */
    public void login(String username, String password)
            throws UserNotExistsException, AlreadyLoggedException, WrongPasswordException, CommunicationException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.LOGIN_CMD,
                username,
                password
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) { // casi di errori
            case CommunicationProtocol.USER_NOT_EXISTS -> throw new UserNotExistsException();
            case CommunicationProtocol.LOGIN_WRONGPWD -> throw new WrongPasswordException();
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.LOGIN_ALREADY_LOGGED  -> throw new AlreadyLoggedException();
        }

        // è andato tutto bene
        try {
            // salvo la risposta del server
            this.userStatus = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<Map<String, UserStatus>>() {
                    }
            );
            // deve rimanere synchronized
            this.userStatus = Collections.synchronizedMap(this.userStatus);

            // istanzia callback
            this.callbackNotify = new RMICallbackNotifyImpl(this.userStatus);

            // richiedo registrazione a servizio di callback
            this.registerForCallback();
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }

        // sono online
        this.username = username;
        this.isLogged = true;
    }

    public void logout()
            throws UserNotExistsException, CommunicationException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.LOGOUT_CMD
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        // casi di errori
        if (response.getStatusCode() == CommunicationProtocol.USER_NOT_EXISTS) {
            throw new UserNotExistsException();
        }

        // richiedo de-registrazione a servizio di callback
        try {
            this.unregisterForCallback();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        // non sono più online
        this.username = "";
        this.isLogged = false;

        // shutdown threads lettori delle chat
        shutdownThreadPool();

        // invalido lista degli indirizzi multicast
        this.projectChatAddresses = new HashMap<>();
    }

    public Map<String, UserStatus> listUsers() {
        return new HashMap<>(this.userStatus);
    }

    public List<String> listOnlineUsers() {
        Map<String, UserStatus> temp = new HashMap<>(this.userStatus);
        List<String> toReturn = new ArrayList<>();
        Set<String> keySet = temp.keySet();
        for (String key : keySet) {
            if (temp.get(key) == UserStatus.ONLINE) {
                toReturn.add(key);
            }
        }
        return toReturn;
    }

    public List<Project> listProjects()
            throws CommunicationException, UserNotExistsException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.LISTPROJECTS_CMD
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        if (response.getStatusCode() == CommunicationProtocol.USER_NOT_EXISTS) {
            throw new UserNotExistsException();
        }

        try {
            List<Project> projectList = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<List<Project>>() {
                    }
            );

            /*
            * invalido dalla map degli indirizzi multicast
            * 1) tutti i progetti eliminati
            * 2) tutti i progetti aventi lo stesso nome di progetti eliminati
            * di cui l'utente faceva parte
            *
            * Nel secondo caso, si tratta di progetti creati dopo l'ultima volta
            * che ho chiamato questo metodo
            */
            List<String> projectNames = new ArrayList<>();
            for (Project project : projectList) {
                projectNames.add(project.getName());
            }
            Set<String> addressKeys = this.projectChatAddresses.keySet();
            for (String addressKey : addressKeys) {
                int index = projectNames.indexOf(addressKey);
                if (index == -1) { // caso 1
                    this.projectChatAddresses.replace(addressKey, null);
                } else {
                    // caso 2
                    if (this.lastListProjectsCall != null) {
                        Project project = projectList.get(index);
                        if (this.lastListProjectsCall.isBefore(project.getCreationDateTime())) {
                            this.projectChatAddresses.replace(addressKey, null);
                        }
                    }
                }
            }

            // aggiorno variabile di ultima chiamata
            this.lastListProjectsCall = LocalDateTime.now(CommunicationProtocol.ZONE_ID);

            return projectList;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public void createProject(String projectName)
            throws ProjectAlreadyExistsException, NoSuchAddressException, CharactersNotAllowedException, CommunicationException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.CREATEPROJECT_CMD,
                projectName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) { // casi di errori
            case CommunicationProtocol.CREATEPROJECT_ALREADYEXISTS -> throw new ProjectAlreadyExistsException();
            case CommunicationProtocol.CREATEPROJECT_NOMOREADDRESSES -> throw new NoSuchAddressException();
            case CommunicationProtocol.CHARS_NOT_ALLOWED -> throw new CharactersNotAllowedException();
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
        }
    }

    public void addMember(String projectName, String username)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, UserAlreadyPresentException, UserNotExistsException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.ADD_MEMBER_CMD,
                projectName,
                username
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.ADD_MEMBER_ALREADYPRESENT -> throw new UserAlreadyPresentException();
            case CommunicationProtocol.USER_NOT_EXISTS -> throw new UserNotExistsException();
        }
    }

    public List<String> showMembers(String projectName)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.SHOW_MEMBERS_CMD,
                projectName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
        }

        try {
            List<String> members = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<List<String>>() {}
            );

            return members;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public Map<CardStatus, List<String>> showCards(String projectName)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.SHOW_CARDS_CMD,
                projectName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
        }

        try {
            Map<CardStatus, List<String>> cards = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<Map<CardStatus, List<String>>>() {}
            );

            return cards;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public CardNoMovs showCard(String projectName, String cardName)
            throws CommunicationException, ProjectNotExistsException, CardNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.SHOW_CARD_CMD,
                projectName,
                cardName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.CARD_NOT_EXISTS -> throw new CardNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
        }

        try {
            CardNoMovs card = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<Card>() {}
            );

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public void addCard(String projectName, String cardName, String description)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, CardAlreadyExistsException, CharactersNotAllowedException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.ADD_CARD_CMD,
                projectName,
                cardName,
                description
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.CHARS_NOT_ALLOWED -> throw new CharactersNotAllowedException();
            case CommunicationProtocol.ADD_CARD_ALREADYEXISTS -> throw new CardAlreadyExistsException();
        }
    }

    public void moveCard(String projectName, String cardName, CardStatus from, CardStatus to)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, OperationNotAllowedException, CardNotExistsException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.MOVE_CARD_CMD,
                projectName,
                cardName,
                from.name(),
                to.name()
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.CARD_NOT_EXISTS -> throw new CardNotExistsException();
            case CommunicationProtocol.MOVE_CARD_NOT_ALLOWED -> throw new OperationNotAllowedException();
        }
    }

    public List<Movement> getCardHistory(String projectName, String cardName)
            throws CommunicationException, CardNotExistsException, UnauthorizedUserException, ProjectNotExistsException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.CARD_HISTORY_CMD,
                projectName,
                cardName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.CARD_NOT_EXISTS -> throw new CardNotExistsException();
        }

        try {
            List<Movement> movements = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<List<Movement>>() {}
            );

            return movements;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public String readChat(String projectName)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // se lo ho già, non devo chiedere al server l'indirizzo multicast
        String chatAddress;
        chatAddress = this.projectChatAddresses.get(projectName);
        if (chatAddress != null) {
            // dico al controller che c'è già un thread in ascolto
            return null;
        }

        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.READ_CHAT_CMD,
                projectName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
        }

        try {
            chatAddress = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<String>() {}
            );

            // salvo la corrispondenza progetto-indirizzo
            this.projectChatAddresses.put(projectName, chatAddress);

            // invio al controller l'indirizzo multicast su cui iniziare ad ascoltare
            return chatAddress;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public void sendChatMsg(String projectName, String message)
            throws UnobtainableChatAddressException, IOException, DatagramTooBigException {
        String chatAddress = this.projectChatAddresses.get(projectName);
        if (chatAddress == null) {
            // non dovrebbe accadere mai, siccome posso scrivere sulla chat
            // solo se la sto guardando, ergo conosco il suo indirizzo multicast
            throw new UnobtainableChatAddressException();
        }
        InetAddress group = InetAddress.getByName(chatAddress);

        UDPMessage udpMessage = new UDPMessage(
                this.username,
                message,
                false
        );
        byte[] byteMessage = this.mapper.writeValueAsBytes(udpMessage);

        // check grandezza messaggio
        if (byteMessage.length >= CommunicationProtocol.UDP_MSG_MAX_LEN)
            throw new DatagramTooBigException();

        DatagramPacket packet = new DatagramPacket(
                byteMessage,
                byteMessage.length,
                group,
                CommunicationProtocol.UDP_CHAT_PORT
        );

        multicastSocket.send(packet);
    }

    public void cancelProject(String projectName)
            throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, ProjectNotCancelableException {
        // prepara messaggio da inviare
        RequestMessage requestMessage = new RequestMessage(
                CommunicationProtocol.CANCELPROJECT_CMD,
                projectName
        );

        ResponseMessage response = this.sendTCPRequest(requestMessage);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.CANCELPROJECT_NOTCANCELABLE -> throw new ProjectNotCancelableException();
        }
    }

    public MulticastSocket getMulticastSocket() {
        return this.multicastSocket;
    }

    public ExecutorService getThreadPool() {
        return this.threadPool;
    }

    public String getUsername() {
        return this.username;
    }

    /**
     * Termina tutti i thread incaricati a ricevere i messaggi delle chat multicast
     */
    public void shutdownThreadPool() {
        try {
            this.threadPool.shutdownNow();

            while (!this.threadPool.isTerminated()) {
                // ogni secondo torno a controllare
                this.threadPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Invia una richiesta TCP al server e attende la risposta da quest'ultimo
     *
     * @param requestMessage messaggio da inviare al server
     *
     * @return messaggio di risposta dal server
     *
     * @throws CommunicationException se ci sono errori di comunicazione
     * */
    private ResponseMessage sendTCPRequest(RequestMessage requestMessage) throws CommunicationException {
        try {
            // converto messaggio in string
            String stringRequest = this.mapper.writeValueAsString(requestMessage);

            // stampa di log
            System.out.println("Call:\n" + stringRequest);

            // preparo messaggio
            byte[] byteMessage = stringRequest.getBytes(StandardCharsets.UTF_8);

            // ottengo la sua lunghezza
            int messageLength = byteMessage.length;

            // scrivo nel buffer la sua lunghezza e il messaggio
            ByteBuffer sendBuffer = ByteBuffer.allocate(Integer.BYTES + messageLength);
            sendBuffer.putInt(messageLength).put(byteMessage);
            sendBuffer.flip();
            while(sendBuffer.hasRemaining())
                socket.write(sendBuffer);
            sendBuffer.clear();

            // attendo risposta server
            ByteBuffer readBuffer = ByteBuffer.allocate(ALLOCATION_SIZE);
            int byteReaded;
            int totalReaded = 0;
            messageLength = -1;
            StringBuilder responseMessage = new StringBuilder();
            do {
                byteReaded = socket.read(readBuffer);
                // se ci sono errori, lancio eccezione
                if (byteReaded == -1)
                    throw new CommunicationException();

                totalReaded += byteReaded;

                readBuffer.flip();

                // salvo lunghezza del messaggio
                if (messageLength == -1)
                    messageLength = readBuffer.getInt();

                responseMessage.append(StandardCharsets.UTF_8.decode(readBuffer).toString());

                readBuffer.clear();
            } while (totalReaded < messageLength);

            String stringResponse = responseMessage.toString();

            // stampa di log
            System.out.println("Response:\n" + stringResponse);

            ResponseMessage response = this.mapper.readValue(stringResponse, new TypeReference<ResponseMessage>() {});
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    /*
     * Operazioni Callback RMI
     */

    /**
     * Registra il client per il servizio di callback
     * */
    private void registerForCallback() throws RemoteException, NotBoundException {
        // realizza connessione RMI per il servizio di callback
        Registry registry = LocateRegistry.getRegistry(CommunicationProtocol.REGISTRY_PORT);
        RMICallbackService callbackService =
                (RMICallbackService) registry.lookup(CommunicationProtocol.CALLBACK_SERVICE_NAME);
        // registrazione al servizio
        callbackService.registerForCallback(this.callbackNotify);
    }

    /**
     * De-registra il client per il servizio di callback
     * */
    private void unregisterForCallback() throws RemoteException, NotBoundException {
        // realizza connessione RMI per il servizio di callback
        Registry registry = LocateRegistry.getRegistry(CommunicationProtocol.REGISTRY_PORT);
        RMICallbackService callbackService =
                (RMICallbackService) registry.lookup(CommunicationProtocol.CALLBACK_SERVICE_NAME);
        // registrazione al servizio
        callbackService.unregisterForCallback(this.callbackNotify);
    }

}
