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

    private CardLayout cardLayout;
    private JPanel cardContainer;

    public ChatPanel() {
        this.messageCards = new HashMap<>();

        // gestione delle Card ChatLog
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
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
}
