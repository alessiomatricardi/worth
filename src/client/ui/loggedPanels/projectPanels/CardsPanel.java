package worth.client.ui.loggedPanels.projectPanels;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 08/01/21
 */
public class CardsPanel extends JPanel {
    private JPanel addCardPanel;
    private JPanel cardsPanel;
    private JScrollPane todoScrollPane;
    private JScrollPane inProgressScrollPane;
    private JScrollPane toBeRevisedScrollPane;
    private JScrollPane doneScrollPane;
    private JTextField addCardField;
    private JButton addCardButton;

    public CardsPanel() {
        this.setLayout(new BorderLayout());

        // dove aggiungo card
        //addCardPanel.setLayout(new FlowLayout());


        // dove le visualizzo tutte

        // aggiungi componenti
        //this.add(addCardPanel,BorderLayout.NORTH);
        //this.add(cardsPanel, BorderLayout.CENTER);
    }
}
