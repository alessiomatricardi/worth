package worth.client.ui.loggedPanels.projectPanels;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 13/01/21
 */
public class AddCardPanel extends JPanel {
    private JTextField cardName;
    private JTextField cardDescription;
    private JButton addCardButton;

    public AddCardPanel() {
        // layout BorderLayout
        this.setLayout(new BorderLayout());

        cardName = new JTextField();
        cardDescription = new JTextField();
        addCardButton = new JButton("Add card");

        this.add(cardName, BorderLayout.NORTH);
        this.add(cardDescription, BorderLayout.CENTER);
        this.add(addCardButton, BorderLayout.SOUTH);
    }

    public JTextField getCardName() {
        return cardName;
    }

    public JTextField getCardDescription() {
        return cardDescription;
    }

    public JButton getAddCardButton() {
        return addCardButton;
    }

}
