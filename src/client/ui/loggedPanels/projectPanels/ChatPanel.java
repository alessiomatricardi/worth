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
    Map<String, ChatCard> messageCards;

    public ChatPanel() {
        this.messageCards = new HashMap<>();
    }

    public void addChatCard(String projectName, ChatCard chatCard) {
        this.messageCards.put(projectName, chatCard);
    }

    @Override
    public CardLayout getCardLayout() {
        return null;
    }

    @Override
    public JPanel getCardContainer() {
        return null;
    }
}
