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
    private String cardName;
    private CardStatus fromStatus;
    private JComboBox<CardStatus> toStatusComboBox;
    private JButton moveCardButton;

    public CardDetailsPanel() {
        // questo componente è diviso in 2 parti
        // nella prima c'è la lista dei movimenti della card
        // nella seconda la possibilità di spostare la card
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
        moveCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // aggiungi buttons al container
        JLabel label = new JLabel("Move card");
        Font font = label.getFont();
        label.setFont(new Font(font.getName(), Font.BOLD, (int)(font.getSize() * 1.5)));
        toStatusComboBox = new JComboBox<>(CardStatus.values());
        moveCardButton = new JButton("Move card");
        JPanel moveContainer = new JPanel(new GridLayout(3, 1, 10, 10));
        moveContainer.add(label);
        moveContainer.add(toStatusComboBox);
        moveContainer.add(moveCardButton);
        moveCardPanel.add(moveContainer);

        // aggiungo componenti
        this.add(movementsScrollPane, BorderLayout.WEST);
        this.add(moveContainer, BorderLayout.EAST);
    }
/*
    public void setUI(List<JLabel> movements) {
        JPanel membersPanel = new JPanel();
        membersPanel.setLayout(new GridLayout(0, 1, 0, 0));

        // inserisco gli utenti nel pannello
        String text = members.size() + " member" + (members.size() == 1 ? "" : "s");
        JLabel numOfMembers = new JLabel(text);
        Font font = numOfMembers.getFont();
        numOfMembers.setFont(new Font(font.getName(), Font.BOLD, (int)(font.getSize() * 1.5)));
        membersPanel.add(numOfMembers);

        for (JLabel member : members) {
            member.setFont(new Font(font.getName(), Font.PLAIN, (int)(font.getSize() * 1.3)));
            member.setPreferredSize(new Dimension(10, 30));
            membersPanel.add(member);
        }

        // container contiene il pannello degli utenti
        JPanel container = new JPanel(new BorderLayout(0,0));
        container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        container.add(membersPanel, BorderLayout.NORTH);

        membersScrollPane.setViewportView(container);
    }
*/
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
}
