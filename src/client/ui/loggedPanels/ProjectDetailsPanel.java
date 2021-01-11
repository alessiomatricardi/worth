package worth.client.ui.loggedPanels;

import worth.client.ui.HostsCardsContainer;
import worth.client.ui.loggedPanels.projectPanels.CardDetailPanel;
import worth.client.ui.loggedPanels.projectPanels.ChatPanel;
import worth.client.ui.loggedPanels.projectPanels.ProjectCardsPanel;
import worth.client.ui.loggedPanels.projectPanels.ProjectMembersPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 08/01/21
 *
 * Pannello di dettaglio di un progetto
 */
public class ProjectDetailsPanel extends JPanel implements HostsCardsContainer {
    public static final String MEMBERS_PANEL = "members";
    public static final String CARDS_PANEL = "cards";
    public static final String CARD_DETAILS_PANEL = "cardDetails";
    public static final String CHAT_PANEL = "chat";

    // componenti
    private final JPanel headerPanel;
    private final JPanel buttonsPanel;
    private final JPanel containerPanel;

    // labels
    private final JLabel projectNameLabel;

    // buttons
    private final JButton cancelButton;
    private final JButton membersButton;
    private final JButton cardsButton;
    private final JButton chatButton;

    // gestore layout
    private final CardLayout cardLayout;

    // panels gestiti in containerPanel
    private ProjectMembersPanel projectMembersPanel;
    private ProjectCardsPanel projectCardsPanel;
    private CardDetailPanel cardDetailPanel;
    private ChatPanel chatPanel;

    public ProjectDetailsPanel() {
        this.setLayout(new BorderLayout());

        // headerPanel ha layout FlowLayout
        headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // aggiungo label e buttons a headerPanel
        projectNameLabel = new JLabel();
        cancelButton = new JButton("Cancel project");
        headerPanel.add(projectNameLabel);
        headerPanel.add(cancelButton);

        // button panel ha layout FlowLayout
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // aggiungo buttons a buttonsPanel
        membersButton = new JButton("Show members");
        cardsButton = new JButton("Show cards");
        chatButton = new JButton("Read chat");
        buttonsPanel.add(membersButton);
        buttonsPanel.add(cardsButton);
        buttonsPanel.add(chatButton);

        // containerPanel ha layout CardLayout
        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        // aggiungo panels al container
        projectMembersPanel = new ProjectMembersPanel();
        projectCardsPanel = new ProjectCardsPanel();
        cardDetailPanel = new CardDetailPanel();
        chatPanel = new ChatPanel();
        containerPanel.add(projectMembersPanel, MEMBERS_PANEL);
        containerPanel.add(projectCardsPanel, CARDS_PANEL);
        containerPanel.add(cardDetailPanel, CARD_DETAILS_PANEL);
        containerPanel.add(chatPanel, CHAT_PANEL);

        // aggiungo pannelli principali
        this.add(buttonsPanel, BorderLayout.NORTH);
        this.add(containerPanel);
    }

    public void setUI(String projectName) {

    }

    @Override
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    @Override
    public JPanel getContainerPanel() {
        return containerPanel;
    }
}
