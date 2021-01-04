package worth.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import worth.CommunicationProtocol;
import worth.ResponseMessage;
import worth.exceptions.*;
import worth.server.RMIRegistrationService;

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

/**
 * Created by alessiomatricardi on 03/01/21
 */
public class ClientModel {
    private static final int ALLOCATION_SIZE = 1024*1024; // spazio di allocazione del buffer
    private String username;
    private SocketChannel socket;
    private ObjectMapper mapper;

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
            throws UserNotExistsException, WrongPasswordException, CommunicationException {
        String messageToSend = this.encodeMessageArguments(
                CommunicationProtocol.LOGIN_CMD,
                username,
                password
        );
        ResponseMessage response = null;
        try {
            response = this.sendTCPMessage(messageToSend);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommunicationException();
        }
        switch (response.getStatusCode()) {
            case CommunicationProtocol.LOGIN_USERNOTEXISTS -> throw new UserNotExistsException();
            case CommunicationProtocol.LOGIN_WRONGPWD -> throw new WrongPasswordException();
            case CommunicationProtocol.LOGIN_COMMUNICATION_ERROR  -> throw new CommunicationException();
        }
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
    private ResponseMessage sendTCPMessage(String messageToSend) throws IOException {
        byte[] byteMessage = messageToSend.getBytes(StandardCharsets.UTF_8);
        ByteBuffer sendBuffer = ByteBuffer.wrap(byteMessage);
        socket.write(sendBuffer);
        sendBuffer.clear();

        // attendo risposta server
        ByteBuffer readBuffer = ByteBuffer.allocate(ALLOCATION_SIZE);
        socket.read(readBuffer);
        readBuffer.flip();
        String stringResponse = StandardCharsets.UTF_8.decode(readBuffer).toString();
        System.out.println(stringResponse);
        ResponseMessage response = this.mapper.readValue(stringResponse, new TypeReference<ResponseMessage>() {});
        return response;
    }
}
