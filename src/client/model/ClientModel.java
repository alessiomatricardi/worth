package worth.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import worth.client.model.rmi.RMICallbackNotify;
import worth.client.model.rmi.RMICallbackNotifyImpl;
import worth.data.*;
import worth.protocol.CommunicationProtocol;
import worth.protocol.ResponseMessage;
import worth.exceptions.*;
import worth.server.rmi.RMICallbackService;
import worth.server.rmi.RMIRegistrationService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
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
    private boolean isLogged;                           // l'utente è loggato?
    private String username;                            // per tenere traccia dello username dell'utente
    private final SocketChannel socket;                 // socket per instaurazione connessione
    private final ObjectMapper mapper;                  // mapper per serializzazione/deserializzazione
    private Map<String, UserStatus> userStatus;         // lista degli stati degli utenti
    private RMICallbackNotify callbackNotify;           // gestione callback
    private Map<String, String> projectChatAddresses;   // indirizzi multicast dei progetti
    private ExecutorService threadPool;                 // threadpool per servizio di lettura chat
    private MulticastSocket multicastSocket;            // socket per chat multicast

    // predispone la connessione del client con il server
    public ClientModel() throws IOException {
        // apre connessione TCP con il server
        this.socket = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(
                CommunicationProtocol.SERVER_IP_ADDRESS,
                CommunicationProtocol.SERVER_PORT
        );
        this.socket.connect(address); // bloccante per il client

        this.mapper = new ObjectMapper();
        // abilita indentazione
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // formattazione data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        this.mapper.setDateFormat(dateFormat);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.userStatus = null; // non è ancora il momento di inizializzarlo
        this.callbackNotify = null; // non è ancora il momento di inizializzarlo
        this.projectChatAddresses = new HashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.multicastSocket = new MulticastSocket(CommunicationProtocol.UDP_CHAT_PORT);
        this.multicastSocket.setSoTimeout(2000); // todo better
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
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.LOGIN_CMD,
                username,
                password
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

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

        // sono loggato
        this.username = username;
        this.isLogged = true;
    }

    public void logout() throws UserNotExistsException, CommunicationException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.LOGOUT_CMD
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

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

        // non sono più loggato
        this.username = "";
        this.isLogged = false;

        // shutdown threads lettori delle chat
        shutdownThreadPool();
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

    public List<Project> listProjects() throws CommunicationException, UserNotExistsException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.LISTPROJECTS_CMD
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        if (response.getStatusCode() == CommunicationProtocol.USER_NOT_EXISTS) {
            throw new UserNotExistsException();
        }

        try {
            List<Project> projectList = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<List<Project>>() {
                    }
            );

            return projectList;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public void createProject(String projectName)
            throws ProjectAlreadyExistsException, NoSuchAddressException, CharactersNotAllowedException, CommunicationException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.CREATEPROJECT_CMD,
                projectName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) { // casi di errori
            case CommunicationProtocol.CREATEPROJECT_ALREADYEXISTS -> throw new ProjectAlreadyExistsException();
            case CommunicationProtocol.CREATEPROJECT_NOMOREADDRESSES -> throw new NoSuchAddressException();
            case CommunicationProtocol.CREATEPROJECT_CHAR_NOT_ALLOW -> throw new CharactersNotAllowedException();
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
        }
    }

    public void addMember(String projectName, String username) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, UserAlreadyPresentException, UserNotExistsException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.ADD_MEMBER_CMD,
                projectName,
                username
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.ADD_MEMBER_ALREADYPRESENT -> throw new UserAlreadyPresentException();
            case CommunicationProtocol.USER_NOT_EXISTS -> throw new UserNotExistsException();
        }
    }

    public List<String> showMembers(String projectName) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.SHOW_MEMBERS_CMD,
                projectName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

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

    public List<String> showCards(String projectName) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.SHOW_CARDS_CMD,
                projectName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
        }

        try {
            List<String> cards = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<List<String>>() {}
            );

            return cards;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public Card showCard(String projectName, String cardName) throws CommunicationException, ProjectNotExistsException, CardNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.SHOW_CARD_CMD,
                projectName,
                cardName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.CARD_NOT_EXISTS -> throw new CardNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
        }

        try {
            Card card = this.mapper.readValue(
                    response.getResponseBody(),
                    new TypeReference<Card>() {}
            );

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

    public void addCard(String projectName, String cardName, String description) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, CardAlreadyExistsException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.ADD_CARD_CMD,
                projectName,
                cardName,
                description
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.ADD_CARD_ALREADYEXISTS -> throw new CardAlreadyExistsException();
        }
    }

    public void moveCard(String projectName, String cardName, CardStatus from, CardStatus to) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException, OperationNotAllowedException, CardNotExistsException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.MOVE_CARD_CMD,
                projectName,
                cardName,
                from.name(),
                to.name()
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
            case CommunicationProtocol.CARD_NOT_EXISTS -> throw new CardNotExistsException();
            case CommunicationProtocol.MOVE_CARD_NOT_ALLOWED -> throw new OperationNotAllowedException();
        }
    }

    public List<Movement> getCardHistory(String projectName, String cardName) throws CommunicationException, CardNotExistsException, UnauthorizedUserException, ProjectNotExistsException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.CARD_HISTORY_CMD,
                projectName,
                cardName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

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

    public String readChat(String projectName) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // se lo ho già, non devo chiedere al server l'indirizzo multicast
        String chatAddress;
        chatAddress = this.projectChatAddresses.get(projectName);
        if (chatAddress != null) {
            // dico al controller che c'è già un thread in ascolto
            return null;
        }

        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.READ_CHAT_CMD,
                projectName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

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

    public void sendChatMsg(String projectName, String messaggio) {
        // todo
    }

    public void cancelProject(String projectName) throws CommunicationException, ProjectNotExistsException, UnauthorizedUserException {
        // prepara messaggio da inviare
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.SHOW_CARDS_CMD,
                projectName
        );

        ResponseMessage response = null;
        response = this.sendTCPRequest(messageToSend);

        switch (response.getStatusCode()) {
            case CommunicationProtocol.COMMUNICATION_ERROR -> throw new CommunicationException();
            case CommunicationProtocol.PROJECT_NOT_EXISTS -> throw new ProjectNotExistsException();
            case CommunicationProtocol.UNAUTHORIZED -> throw new UnauthorizedUserException();
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
                this.threadPool.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @param command comando da eseguire
     * @param args argomenti specifici del comando da eseguire
     *
     * @return comando completo con argomenti codificati in Base64
     * */
    private String encodeMessageArguments(String command, String... args) {
        StringBuilder toReturn = new StringBuilder(command);
        for (String arg : args) {
            String encoded = Base64.getEncoder().encodeToString(arg.getBytes());
            toReturn.append(CommunicationProtocol.SEPARATOR).append(encoded);
        }
        return toReturn.toString();
    }

    /**
     * @param messageToSend messaggio da inviare al server
     *
     * @return messaggio di risposta dal server
     *
     * @throws CommunicationException se ci sono errori di comunicazione
     * */
    private ResponseMessage sendTCPRequest(String messageToSend) throws CommunicationException {
        try {
            // preparo messaggio
            byte[] byteMessage = messageToSend.getBytes(StandardCharsets.UTF_8);
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
                totalReaded += byteReaded;

                readBuffer.flip();

                // salvo lunghezza del messaggio
                if (messageLength == -1)
                    messageLength = readBuffer.getInt();

                responseMessage.append(StandardCharsets.UTF_8.decode(readBuffer).toString());

                readBuffer.clear();
            } while (totalReaded < messageLength);

            String stringResponse = responseMessage.toString();
            System.out.println(stringResponse);
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
