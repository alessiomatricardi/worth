package worth.client.ui.loggedPanels.projectPanels;

import worth.client.ui.HostsCardsContainer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alessiomatricardi on 08/01/21
 */
public class ChatPanel extends JPanel implements HostsCardsContainer {
    // pannelli dove vengono visualizzati i messaggi (uno per chat = uno per progetto)
    // messagePanels.get(nomeProgetto) = pannello chat del progetto
    private Map<String, ChatLog> messageCards;

    private JTextField messageField; // dove scrivere il messaggio
    private JButton sendButton;

    private CardLayout cardLayout;
    private JPanel cardContainer;

    public ChatPanel() {
        // layout BorderLayout
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // istanzio mappa di ChatLog
        this.messageCards = new HashMap<>();

        // gestione delle Card ChatLog
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        // parte sottostante, dove vengono scritti i messaggi e inviati
        messageField = new JTextField();
        sendButton = new JButton("Send");
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        this.add(cardContainer, BorderLayout.CENTER);
        this.add(messagePanel, BorderLayout.SOUTH);
    }

    public void addChatLog(String projectName, ChatLog chatLog) {
        // inserisco nella map la correlazione <nome progetto, chat log del progetto>
        this.messageCards.put(projectName, chatLog);
        // inserisco nel container questa nuova Card
        this.cardContainer.add(chatLog, projectName);
    }

    @Override
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    @Override
    public JPanel getCardContainer() {
        return cardContainer;
    }

    public JTextField getMessageField() {
        return messageField;
    }

    public JButton getSendButton() {
        return sendButton;
    }
}
