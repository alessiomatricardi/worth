package worth.client;

import worth.client.ui.loggedPanels.ChatMessageList;
import worth.protocol.UDPMessage;

import javax.swing.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Created by alessiomatricardi on 11/01/21
 *
 * Thread che si occupa di leggere la chat e popolare la UI con i messaggi arrivati
 */
public class ReadChatTask implements Runnable {
    private final String applicationUser;           // utente online sull'applicativo
    private final MulticastSocket socket;           // socket
    private final InetAddress groupAddress;         // indirizzo multicast
    private final int port;                         // porta dove ricevere i messaggi
    private final ChatMessageList chatMessageList;  // dove aggiungere i messaggi

    public ReadChatTask(String user,
                        MulticastSocket socket,
                        String address, int port,
                        ChatMessageList chatMessageList
    ) throws UnknownHostException {
        this.applicationUser = user;
        this.socket = socket;
        this.groupAddress = InetAddress.getByName(address);
        this.port = port;
        this.chatMessageList = chatMessageList;
    }

    @Override
    public void run() {
        while(true) { // finchè non interrotto dal threadpool
            // todo
        }
    }

    public JComponent getMessageUI (UDPMessage message) {
        return null; // todo
    }

}
