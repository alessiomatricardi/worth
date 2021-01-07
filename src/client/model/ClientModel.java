package worth.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import worth.client.model.rmi.RMICallbackNotify;
import worth.client.model.rmi.RMICallbackNotifyImpl;
import worth.data.UserStatus;
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
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Model (logica) del client secondo il pattern MVC
 */
public class ClientModel {
    private static final int ALLOCATION_SIZE = 1024*1024; // spazio di allocazione del buffer
    private boolean isLogged; // l'utente è loggato?
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
    }

    public void closeConnection() {
        if (!this.isLogged) return;
        try {
            logout();
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        this.isLogged = false;
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
            byte[] byteMessage = messageToSend.getBytes(StandardCharsets.UTF_8);
            ByteBuffer sendBuffer = ByteBuffer.wrap(byteMessage);
            socket.write(sendBuffer);
            sendBuffer.clear();

            // attendo risposta server
            ByteBuffer readBuffer = ByteBuffer.allocate(ALLOCATION_SIZE);
            socket.read(readBuffer);
            /* todo guarda non bloccante
             * int total = 0;
             * while (true) {
             *   int readed = socket.read(readBuffer);
             *   total += readed;
             *   if (readed == 0 && total > 0) break;
             * }
             * */
            readBuffer.flip();
            String stringResponse = StandardCharsets.UTF_8.decode(readBuffer).toString();
            System.out.println(stringResponse);
            ResponseMessage response = this.mapper.readValue(stringResponse, new TypeReference<ResponseMessage>() {
            });
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
    }

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
