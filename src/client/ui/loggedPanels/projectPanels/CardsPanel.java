package worth.client.ui.loggedPanels.projectPanels;

import worth.data.CardStatus;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alessiomatricardi on 08/01/21
 *
 * Pannello che mostra le card di un progetto
 */
public class CardsPanel extends JPanel {
    private Map<CardStatus, JScrollPane> scrollPanes;

    public CardsPanel() {
        // una colonna per card
        this.setLayout(new GridLayout(1,4));

        scrollPanes = new HashMap<>();
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            JScrollPane scrollPane = new JScrollPane(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            );
            // aumenta velocità dello scorrimento
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            // niente bordi
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            scrollPanes.put(status, scrollPane);

            // lo aggiungo al GridLayout
            this.add(scrollPane);
        }
    }

    public void setUI(Map<CardStatus, List<JButton>> cards) {
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            List<JButton> buttonList = cards.get(status);
            if (buttonList != null) {
                JPanel membersPanel = new JPanel();
                membersPanel.setLayout(new GridLayout(0, 1, 0, 0));

                // inserisco i buttons nel pannello

                for (JButton button : buttonList) {
                    Font font = button.getFont();
                    button.setFont(new Font(font.getName(), Font.PLAIN, (int)(font.getSize() * 1.3)));
                    button.setPreferredSize(new Dimension(300, 40));
                    membersPanel.add(button);
                }

                // container contiene il pannello degli utenti
                JPanel container = new JPanel(new BorderLayout(0,0));
                container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                container.add(membersPanel, BorderLayout.NORTH);

                this.scrollPanes.get(status).setViewportView(container);
            }
        }
    }

}
