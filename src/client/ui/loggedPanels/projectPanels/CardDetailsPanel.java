package worth.client.ui.loggedPanels.projectPanels;

import worth.data.CardStatus;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by alessiomatricardi on 08/01/21
 *
 * Pannello che permette di visualizzare tutti i movimenti di una carta e spostarla
 */
public class CardDetailsPanel extends JPanel {
    private JScrollPane movementsScrollPane;
    private JPanel moveCardPanel;
    private JPanel descriptionPanel;
    private String cardName = "";
    private String description = "";
    private CardStatus fromStatus = CardStatus.TODO;
    private JComboBox<CardStatus> toStatusComboBox;
    private JButton moveCardButton;
    JLabel moveCardLabel;
    JLabel fromStateLabel;
    JLabel descriptionLabel;

    public CardDetailsPanel() {
        // questo componente è diviso in 2 parti
        // nella prima c'è la lista dei movimenti della card
        // nella seconda la possibilità di spostare la card e la descrizione
        this.setLayout(new GridLayout(1,2));

        // istanza containers
        // layout scrollabile, se necessario
        movementsScrollPane = new JScrollPane(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        // aumenta velocità dello scorrimento
        movementsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // niente bordi
        movementsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // il panel di destra è diviso in 2 righe
        JPanel moveAndDescriptionPanel = new JPanel(new GridLayout(2, 1));

        moveCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        descriptionPanel = new JPanel(new BorderLayout());

        // aggiungi elementi al moveCardPanel

        moveCardLabel = new JLabel("", JLabel.CENTER);
        fromStateLabel = new JLabel("", JLabel.CENTER);
        JLabel toStateLabel = new JLabel("To state:", JLabel.CENTER);
        Font font = moveCardLabel.getFont();
        Font newFont = new Font(font.getName(), Font.BOLD, (int)(font.getSize() * 1.5));
        moveCardLabel.setFont(newFont);
        fromStateLabel.setFont(newFont);
        toStateLabel.setFont(newFont);
        JPanel labelPanel = new JPanel(new GridLayout(3,1));
        labelPanel.add(moveCardLabel);
        labelPanel.add(fromStateLabel);
        labelPanel.add(toStateLabel);

        toStatusComboBox = new JComboBox<>(CardStatus.values());
        moveCardButton = new JButton("Move card");
        JPanel moveContainer = new JPanel(new GridLayout(3, 1, 10, 10));
        moveContainer.add(labelPanel);
        moveContainer.add(toStatusComboBox);
        moveContainer.add(moveCardButton);

        moveCardPanel.add(moveContainer);

        // aggiungi elementi al descriptionPanel
        JLabel descriptionTitle = new JLabel("Card description");
        descriptionTitle.setFont(newFont);
        descriptionLabel = new JLabel();
        descriptionLabel.setVerticalAlignment(JLabel.TOP);

        descriptionPanel.add(descriptionTitle, BorderLayout.NORTH);
        descriptionPanel.add(descriptionLabel, BorderLayout.CENTER);

        // aggiungo moveCardPanel e descriptionPanel a moveAndDescriptionPanel
        moveAndDescriptionPanel.add(moveCardPanel);
        moveAndDescriptionPanel.add(descriptionPanel);

        // aggiungo componenti al panel principale
        this.add(movementsScrollPane, BorderLayout.WEST);
        this.add(moveAndDescriptionPanel, BorderLayout.EAST);
    }

    public void setUI(List<JPanel> movements) {
        moveCardLabel.setText("Move card " + this.cardName);
        fromStateLabel.setText("From state: " + this.fromStatus.name());
        descriptionLabel.setText(this.description);

        JPanel movementsPanel = new JPanel();
        movementsPanel.setLayout(new GridLayout(0, 1, 0, 0));

        // inserisco gli utenti nel pannello
        String text = movements.size() + " movement" + (movements.size() == 1 ? "" : "s");
        JLabel numOfMovs = new JLabel(text);
        Font font = numOfMovs.getFont();
        numOfMovs.setFont(new Font(font.getName(), Font.BOLD, (int)(font.getSize() * 1.5)));
        movementsPanel.add(numOfMovs);

        for (JPanel mov : movements) {
            mov.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            movementsPanel.add(mov);
        }

        // container contiene il pannello degli utenti
        JPanel container = new JPanel(new BorderLayout(0,0));
        container.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));
        container.add(movementsPanel, BorderLayout.NORTH);

        movementsScrollPane.setViewportView(container);
    }

    public String getCardName() {
        return cardName;
    }

    public CardStatus getFromStatus() {
        return fromStatus;
    }

    public JComboBox<CardStatus> getToStatusComboBox() {
        return toStatusComboBox;
    }

    public JButton getMoveCardButton() {
        return moveCardButton;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setFromStatus(CardStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
