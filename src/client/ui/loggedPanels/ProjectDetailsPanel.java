package worth.client.ui.loggedPanels;

import worth.client.ui.HostsCardsContainer;
import worth.client.ui.loggedPanels.projectPanels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 08/01/21
 *
 * Pannello di dettaglio di un progetto
 */
public class ProjectDetailsPanel extends JPanel implements HostsCardsContainer {
    // nomi delle cards
    public static final String MEMBERS_PANEL = "members";
    public static final String CARDS_PANEL = "cards";
    public static final String ADD_CARD_PANEL = "addCard";
    public static final String CARD_DETAILS_PANEL = "cardDetails";
    public static final String CHAT_PANEL = "chat";

    // componenti
    private final JPanel headerPanel;
    private final JPanel buttonsPanel;
    private final JPanel cardContainer;

    // labels
    private final JLabel projectNameLabel;

    // buttons
    private final JButton cancelButton;
    private final JButton membersButton;
    private final JButton cardsButton;
    private final JButton addCardButton;
    private final JButton chatButton;

    // gestore layout
    private final CardLayout cardLayout;

    // panels gestiti in containerPanel
    private MembersPanel membersPanel;
    private CardsPanel cardsPanel;
    private AddCardPanel addCardPanel;
    private CardDetailsPanel cardDetailsPanel;
    private ChatPanel chatPanel;

    public ProjectDetailsPanel() {
        this.setLayout(new BorderLayout());

        // headerPanel ha layout FlowLayout
        headerPanel = new JPanel(new FlowLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // aggiungo label e buttons a headerPanel
        projectNameLabel = new JLabel();
        Font font = projectNameLabel.getFont();
        int newSize = (int) (font.getSize() * 1.3);
        projectNameLabel.setFont(new Font(font.getName(), Font.BOLD, newSize));
        cancelButton = new JButton("Cancel project");
        headerPanel.add(projectNameLabel);
        headerPanel.add(cancelButton);

        JPanel buttonsAndCardContainer = new JPanel(new BorderLayout());

        // button panel ha layout FlowLayout
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // aggiungo buttons a buttonsPanel
        membersButton = new JButton("Show members");
        cardsButton = new JButton("Show cards");
        addCardButton = new JButton("New card");
        chatButton = new JButton("Read chat");
        buttonsPanel.add(membersButton);
        buttonsPanel.add(cardsButton);
        buttonsPanel.add(addCardButton);
        buttonsPanel.add(chatButton);

        // containerPanel ha layout CardLayout
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        // aggiungo panels al container
        membersPanel = new MembersPanel();
        cardsPanel = new CardsPanel();
        cardDetailsPanel = new CardDetailsPanel();
        addCardPanel = new AddCardPanel();
        chatPanel = new ChatPanel();
        cardContainer.add(membersPanel, MEMBERS_PANEL);
        cardContainer.add(cardsPanel, CARDS_PANEL);
        cardContainer.add(cardDetailsPanel, CARD_DETAILS_PANEL);
        cardContainer.add(addCardPanel, ADD_CARD_PANEL);
        cardContainer.add(chatPanel, CHAT_PANEL);

        // aggiungo buttonsPanel e cardContainer
        buttonsAndCardContainer.add(buttonsPanel, BorderLayout.NORTH);
        buttonsAndCardContainer.add(cardContainer);

        // aggiungo pannelli principali
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(buttonsAndCardContainer);
    }

    @Override
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    @Override
    public JPanel getCardContainer() {
        return cardContainer;
    }

    public JLabel getProjectNameLabel() {
        return projectNameLabel;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getMembersButton() {
        return membersButton;
    }

    public JButton getCardsButton() {
        return cardsButton;
    }

    public JButton getAddCardButton() {
        return addCardButton;
    }

    public JButton getChatButton() {
        return chatButton;
    }

    public MembersPanel getMembersPanel() {
        return membersPanel;
    }

    public CardsPanel getCardsPanel() {
        return cardsPanel;
    }

    public CardDetailsPanel getCardDetailsPanel() {
        return cardDetailsPanel;
    }

    public AddCardPanel getAddCardPanel() {
        return addCardPanel;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

}
