package worth.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import worth.client.ui.loggedPanels.projectPanels.ChatMessageList;
import worth.protocol.CommunicationProtocol;
import worth.protocol.UDPMessage;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by alessiomatricardi on 11/01/21
 *
 * Thread che si occupa di leggere la chat e popolare la UI con i messaggi arrivati
 */
public class ChatReaderControllerTask implements Runnable {
    private final String applicationUser;           // utente online sull'applicativo
    private final MulticastSocket socket;           // socket
    private final InetAddress group;                // indirizzo multicast
    private final int port;                         // porta dove ricevere i messaggi
    private final ChatMessageList chatMessageList;  // dove aggiungere i messaggi

    public ChatReaderControllerTask(String user,
                                    MulticastSocket socket,
                                    String address, int port,
                                    ChatMessageList chatMessageList
    ) throws UnknownHostException {
        this.applicationUser = user;
        this.socket = socket;
        this.group = InetAddress.getByName(address);
        this.port = port;
        this.chatMessageList = chatMessageList;
    }

    @Override
    public void run() {
        try {
            socket.joinGroup(group);
            byte[] buffer = new byte[CommunicationProtocol.UDP_MSG_MAX_LEN];
            DatagramPacket packetReceived = new DatagramPacket(
                    buffer,
                    buffer.length,
                    group,
                    port
            );

            // finchè non interrotto dal threadpool oppure progetto cancellato
            boolean isTimeToExit = false;
            while(true) {
                try {
                    socket.receive(packetReceived);

                    String boh = new String(
                            packetReceived.getData(),
                            packetReceived.getOffset(),
                            packetReceived.getLength(),
                            StandardCharsets.UTF_8
                    );

                    UDPMessage udpMessage = new ObjectMapper().readValue(boh,
                            new TypeReference<UDPMessage>() {});

                    System.out.println(udpMessage.getMessage());

                    isTimeToExit = isTimeToExit(udpMessage);
                } catch (SocketTimeoutException e) {
                    // timeout scaduto, torno a cercare messaggi
                }

                if (isTimeToExit || Thread.currentThread().isInterrupted()) {
                    // lascio il gruppo
                    socket.leaveGroup(group);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // costruisce una UI del messaggio in base a diversi parametri
    public JComponent getMessageUI (UDPMessage message) {
        // se il messaggio è del sistema todo
        if (message.isFromSystem()) {
            // costruisce messaggio inviato dal sistema
        } else if (message.getAuthor().equals(this.applicationUser)) {
            // costruisce messaggio inviato da colui che è attualmente online nell'applicativo
        } else {
            // costruisce messaggio inviato da altro membro del progetto
        }
        return null;
    }

    /*
    * se il sistema invia il messaggio di terminazione vuol dire che
    * il progetto è stato cancellato, per cui la chat deve terminare
    * */
    public boolean isTimeToExit(UDPMessage message) {
        return message.isFromSystem()
                && message.getMessage().equals(CommunicationProtocol.UDP_TERMINATE_MSG);
    }

}
