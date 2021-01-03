package worth.client.model;

import worth.CommunicationProtocol;
import worth.server.RMIRegistrationService;
import worth.server.RegistrationTask;
import worth.server.SelectionTask;
import worth.exceptions.PasswordTooShortException;
import worth.exceptions.CharactersNotAllowedException;
import worth.exceptions.UsernameNotAvailableException;

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
    private final static String SUCCESS_CODE = "0";
    private String username;
    private SocketChannel socket;

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

    public void login(String username, String password) throws Exception {
        String messageToSend = this.encodeMessageParams("login", username, password); // todo macro for commands
        String response = this.sendTCPMessage(messageToSend);
        if (!response.startsWith(SUCCESS_CODE)) { // todo macros???
            throw new Exception(response);
        }
    }

    // todo interface
    private String encodeMessageParams(String command, String... params) {
        StringBuilder toReturn = new StringBuilder(command);
        for (String param : params) {
            String encoded = Base64.getEncoder().encodeToString(param.getBytes());
            toReturn.append(" ").append(encoded);
        }
        return toReturn.toString();
    }

    // todo interface
    private String sendTCPMessage(String messageToSend) throws IOException {
        byte[] byteMessage = messageToSend.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(byteMessage);
        socket.write(buffer);
        buffer.clear();

        // attendo risposta server
        StringBuilder response = new StringBuilder();
        while(socket.read(buffer) > 0) {
            buffer.flip();

            response.append(StandardCharsets.UTF_8.decode(buffer).toString());

            buffer.clear();
        }
        return response.toString();
    }
}
