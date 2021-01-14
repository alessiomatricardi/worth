package worth.client.ui.loggedPanels.projectPanels;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 13/01/21
 */
public class AddCardPanel extends JPanel {
    private JTextField cardName;
    private JTextArea cardDescription;
    private JButton addCardButton;

    public AddCardPanel() {
        // layout BorderLayout
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // parte superiore
        JPanel cardNamePanel = new JPanel(new FlowLayout());

        JLabel cardNameTitle = new JLabel("Card name ");
        cardName = new JTextField();
        cardName.setPreferredSize(new Dimension(150, 30));
        cardNamePanel.add(cardNameTitle);
        cardNamePanel.add(cardName);

        // parte centrale
        JPanel cardDescriptionPanel = new JPanel(new BorderLayout());

        JLabel cardDescriptionTitle = new JLabel("Description", JLabel.CENTER);
        cardDescriptionTitle.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        cardDescription = new JTextArea();
        //cardDescription.setAlignmentY(TOP_ALIGNMENT);
        cardDescriptionPanel.add(cardDescriptionTitle, BorderLayout.NORTH);
        cardDescriptionPanel.add(cardDescription, BorderLayout.CENTER);

        // ultima parte

        addCardButton = new JButton("Add card");

        this.add(cardNamePanel, BorderLayout.NORTH);
        this.add(cardDescriptionPanel, BorderLayout.CENTER);
        this.add(addCardButton, BorderLayout.SOUTH);
    }

    public JTextField getCardName() {
        return cardName;
    }

    public JTextArea getCardDescription() {
        return cardDescription;
    }

    public JButton getAddCardButton() {
        return addCardButton;
    }

}
