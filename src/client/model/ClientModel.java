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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Model (logica) del client secondo il pattern MVC
 */
public class ClientModel {
    private static final int ALLOCATION_SIZE = 512*512; // spazio di allocazione del buffer
    private boolean isLogged;                   // l'utente è loggato?
    private String username;                    // per tenere traccia dello username dell'utente
    private SocketChannel socket;               // socket per instaurazione connessione
    private ObjectMapper mapper;                // mapper per serializzazione/deserializzazione
    private Map<String, UserStatus> userStatus; // lista degli stati degli utenti
    private RMICallbackNotify callbackNotify;   // gestione callback

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

        this.userStatus = Collections.synchronizedMap(new HashMap<>());
        this.callbackNotify = new RMICallbackNotifyImpl(this.userStatus);
        this.isLogged = false;
        this.username = "";
    }

    public void closeConnection() { // todo possibile chiudere threads
        if (!this.isLogged) return;
        try {
            this.unregisterForCallback();
            this.socket.close();
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
    }

    public Map<String, UserStatus> listUsers() {
        return this.userStatus;
    }

    public Map<String, UserStatus> listOnlineUsers() {
        Map<String, UserStatus> toReturn = new HashMap<>(this.userStatus);
        Set<String> keySet = toReturn.keySet();
        for (String key : keySet) {
            if (toReturn.get(key) == UserStatus.OFFLINE) {
                toReturn.remove(key);
            }
        }
        return toReturn;
    }

    public List<Project> listProjects() {
        return null; // todo
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

    public void addMember(String projectName, String username) {
        // todo
    }

    public List<String> showMembers(String projectName) {
        return null; // todo
    }

    public List<String> showCards(String projectName) {
        return null;        // todo
    }

    public Card showCard(String projectName, String cardName) {
        return null;        // todo
    }

    public void addCard(String projectName, String cardName, String descrizione) {
        // todo
    }

    public void moveCard(String projectName, String cardName, CardStatus from, CardStatus to) {
        // todo
    }

    public List<Movement> getCardHistory(String projectName, String cardName) {
        return null;        // todo
    }

    public void readChat(String projectName) {
        // todo
    }

    public void sendChatMsg(String projectName, String messaggio) {
        // todo
    }

    public void cancelProject(String projectName) {
        // todo
    }

    public String getUsername() {
        return this.username;
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
