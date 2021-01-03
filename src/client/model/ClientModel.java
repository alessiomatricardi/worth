package worth.client.model;

import worth.RegistrationService;
import worth.RegistrationTask;
import worth.SelectionTask;
import worth.exceptions.PasswordTooShortException;
import worth.exceptions.SpacesNotAllowedException;
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

/**
 * Created by alessiomatricardi on 03/01/21
 */
public class ClientModel {
    private final static String SUCCESS_CODE = "0";
    private String username;
    private SocketChannel socket;

    public ClientModel() throws IOException {
        this.username = null;
        // apre connessione TCP con il server
        this.socket = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(
                SelectionTask.SERVER_IP_ADDRESS,
                SelectionTask.SERVER_PORT
        );
        this.socket.connect(address); // bloccante per il client
    }

    public void register(String username, String password)
            throws RemoteException, NotBoundException, SpacesNotAllowedException,
            UsernameNotAvailableException, PasswordTooShortException {
        // realizza connessione RMI per il servizio di registrazione
        Registry registry = LocateRegistry.getRegistry(RegistrationTask.REGISTRY_PORT);
        RegistrationService regService = (RegistrationService) registry.lookup(RegistrationService.REGISTRATION_SERVICE_NAME);
        // call al servizio RMI
        regService.register(username, password);
    }

    public void login(String username, String password) throws Exception {
        String messageToSend = "login " + username + " " + password; // todo prevedere whitespaces
        String response = sendTCPCommand(messageToSend);
        if (!response.startsWith(SUCCESS_CODE)) { // todo macros???
            throw new Exception(response);
        }
    }

    private String sendTCPCommand(String messageToSend) throws IOException {
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
