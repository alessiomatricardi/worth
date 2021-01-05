package worth.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alessiomatricardi on 03/01/21
 */
public class ClientModel {
    private static final int ALLOCATION_SIZE = 1024*1024; // spazio di allocazione del buffer
    private String username;
    private SocketChannel socket;
    private ObjectMapper mapper;
    private Map<String, UserStatus> userStatus;
    private RMICallbackNotify callbackNotify;

    // predispone la connessione del client con il server
    public ClientModel() throws IOException {
        this.username = null;
        // apre connessione TCP con il server
        this.socket = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(
                CommunicationProtocol.SERVER_IP_ADDRESS,
                CommunicationProtocol.SERVER_PORT
        );
        this.socket.connect(address); // bloccante per il client
        this.mapper = new ObjectMapper();
        this.userStatus = Collections.synchronizedMap(new HashMap<>());
        this.callbackNotify = new RMICallbackNotifyImpl(this.userStatus);
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
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.LOGIN_CMD,
                username,
                password
        );
        ResponseMessage response = null;
        try {
            response = this.sendTCPRequest(messageToSend);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
        switch (response.getStatusCode()) {
            case CommunicationProtocol.LOGIN_USERNOTEXISTS -> throw new UserNotExistsException();
            case CommunicationProtocol.LOGIN_WRONGPWD -> throw new WrongPasswordException();
            case CommunicationProtocol.LOGIN_COMMUNICATION_ERROR  -> throw new CommunicationException();
            case CommunicationProtocol.LOGIN_ALREADY_LOGGED  -> throw new AlreadyLoggedException();
        }
        // è andato tutto bene
        try {
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
        this.username = username; // salvo username dell'utente loggato
    }

    // todo interface
    private String encodeMessageArguments(String command, String... args) {
        StringBuilder toReturn = new StringBuilder(command);
        for (String arg : args) {
            String encoded = Base64.getEncoder().encodeToString(arg.getBytes());
            toReturn.append(CommunicationProtocol.SEPARATOR).append(encoded);
        }
        return toReturn.toString();
    }

    // todo interface
    private ResponseMessage sendTCPRequest(String messageToSend) throws IOException {
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
        ResponseMessage response = this.mapper.readValue(stringResponse, new TypeReference<ResponseMessage>() {});
        return response;
    }

    // todo interface
    private void registerForCallback() throws RemoteException, NotBoundException {
        // realizza connessione RMI per il servizio di callback
        Registry registry = LocateRegistry.getRegistry(CommunicationProtocol.REGISTRY_PORT);
        RMICallbackService callbackService =
                (RMICallbackService) registry.lookup(CommunicationProtocol.CALLBACK_SERVICE_NAME);
        // registrazione al servizio
        callbackService.registerForCallback(this.callbackNotify);
    }

    // todo interface
    private void unregisterForCallback() throws RemoteException, NotBoundException {
        // realizza connessione RMI per il servizio di callback
        Registry registry = LocateRegistry.getRegistry(CommunicationProtocol.REGISTRY_PORT);
        RMICallbackService callbackService =
                (RMICallbackService) registry.lookup(CommunicationProtocol.CALLBACK_SERVICE_NAME);
        // registrazione al servizio
        callbackService.unregisterForCallback(this.callbackNotify);
    }

}
