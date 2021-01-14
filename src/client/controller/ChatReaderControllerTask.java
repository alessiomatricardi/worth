package worth.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import worth.client.ui.loggedPanels.projectPanels.ChatLog;
import worth.protocol.CommunicationProtocol;
import worth.protocol.UDPMessage;

import javax.swing.*;
import java.awt.*;
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
    private final ChatLog chatLog;                  // dove aggiungere i messaggi

    public ChatReaderControllerTask(String user,
                                    MulticastSocket socket,
                                    String address, int port,
                                    ChatLog chatLog
    ) throws UnknownHostException {
        this.applicationUser = user;
        this.socket = socket;
        this.group = InetAddress.getByName(address);
        this.port = port;
        this.chatLog = chatLog;
    }

    @Override
    public void run() {
        try {
            NetworkInterface ni = socket.getNetworkInterface();
            SocketAddress groupSocket = new InetSocketAddress(group, port);
            socket.joinGroup(groupSocket, ni);
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

                    showMessage(udpMessage);

                    isTimeToExit = isTimeToExit(udpMessage);
                } catch (SocketTimeoutException e) {
                    // timeout scaduto, torno a cercare messaggi
                }

                if (isTimeToExit || Thread.currentThread().isInterrupted()) {
                    // lascio il gruppo
                    socket.leaveGroup(groupSocket, ni);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // costruisce una UI del messaggio in base a diversi parametri
    public void showMessage (UDPMessage message) {
        JPanel panel = new JPanel();
        // se il messaggio è del sistema
        if (message.isFromSystem()) {
            // costruisce messaggio inviato dal sistema
            // messaggio "plain"
            JLabel text = new JLabel(message.getAuthor() + ": " + message.getMessage());
            Font font = text.getFont();
            int newSize = (int) (font.getSize() * 1.1);
            text.setFont(new Font(font.getName(), Font.PLAIN, newSize));
            panel.add(text);
        } else if (message.getAuthor().equals(this.applicationUser)) {
            // costruisce messaggio inviato da colui che è attualmente online nell'applicativo
            panel.setSize(new Dimension(900, 50));
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JPanel inside = new JPanel(new GridLayout(2,1));

            JLabel authorLabel = new JLabel(message.getAuthor());
            Font font = authorLabel.getFont().deriveFont(Font.BOLD);
            authorLabel.setFont(font);

            JLabel textMessage = new JLabel("<html>" + message.getMessage() + "</html>");
            textMessage.setPreferredSize(new Dimension(900, 40));
            textMessage.setVerticalAlignment(JLabel.TOP);

            inside.add(authorLabel);
            inside.add(textMessage);
            panel.add(inside);
        } else {
            // costruisce messaggio inviato da altro membro del progetto
            // come sopra, ma con allineamento a destra
            panel.setSize(new Dimension(900, 50));
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JPanel inside = new JPanel(new GridLayout(2,1));

            JLabel authorLabel = new JLabel(message.getAuthor());
            Font font = authorLabel.getFont().deriveFont(Font.BOLD);
            authorLabel.setFont(font);
            authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JLabel textMessage = new JLabel("<html>" + message.getMessage() + "</html>");
            textMessage.setPreferredSize(new Dimension(900, 40));
            textMessage.setVerticalAlignment(JLabel.TOP);
            textMessage.setHorizontalAlignment(SwingConstants.RIGHT);

            inside.add(authorLabel);
            inside.add(textMessage);
            panel.add(inside);
        }
        this.chatLog.addMessage(panel);
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
