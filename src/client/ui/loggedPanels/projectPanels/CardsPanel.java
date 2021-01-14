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
    private JPanel scrollPaneContainer;
    private JPanel statusNameContainer;

    public CardsPanel() {
        // una colonna per card
        this.setLayout(new BorderLayout());

        // istanza 2 container principali
        scrollPaneContainer = new JPanel(new GridLayout(1,4));
        statusNameContainer = new JPanel(new GridLayout(1,4));
        statusNameContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        CardStatus[] values = CardStatus.values();

        // nella prima riga ci sono i titoli delle liste
        for (CardStatus status : values) {
            JLabel statusNameLabel = new JLabel(status.name());
            Font font = statusNameLabel.getFont();
            int newFontSize = (int) (font.getSize() * 1.5);
            statusNameLabel.setFont(new Font(font.getName(), Font.BOLD, newFontSize));
            statusNameContainer.add(statusNameLabel);
        }

        // nella seconda riga ci sono le liste
        scrollPanes = new HashMap<>();
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
            scrollPaneContainer.add(scrollPane);
        }

        // aggiungo i 2 container principali a questo
        this.add(statusNameContainer, BorderLayout.NORTH);
        this.add(scrollPaneContainer, BorderLayout.CENTER);
    }

    public void setUI(Map<CardStatus, List<JButton>> cards) {
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            List<JButton> buttonList = cards.get(status);
            if (buttonList != null) {
                JPanel cardsPanel = new JPanel();
                cardsPanel.setLayout(new GridLayout(0, 1, 0, 0));

                // inserisco i buttons nel pannello

                for (JButton button : buttonList) {
                    Font font = button.getFont();
                    button.setFont(new Font(font.getName(), Font.PLAIN, (int)(font.getSize() * 1.3)));
                    button.setPreferredSize(new Dimension(100, 40));
                    cardsPanel.add(button);
                }

                // container contiene il pannello degli utenti
                JPanel container = new JPanel(new BorderLayout(0,0));
                container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                container.add(cardsPanel, BorderLayout.NORTH);

                this.scrollPanes.get(status).setViewportView(container);
            }
        }
    }

}
