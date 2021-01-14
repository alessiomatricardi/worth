package worth.client.ui.loggedPanels.projectPanels;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 11/01/21
 *
 * Pannello dove confluiscono tutti i messaggi arrivati sulla chat
 */
public class ChatLog extends JPanel {
    JPanel messageList;

    public ChatLog() {
        // ha borderLayout anche se contiene solo lo scrollPane
        this.setLayout(new BorderLayout());

        messageList = new JPanel(new GridLayout(0, 1, 0, 0));

        // container contiene la lista dei messaggi
        JPanel container = new JPanel(new BorderLayout(0,0));
        container.add(messageList, BorderLayout.NORTH);

        // layout scrollabile se necessario
        JScrollPane scrollPane = new JScrollPane(
                container,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        // aumenta velocità dello scorrimento
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // niente bordi
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void addMessage(JPanel message) {
        messageList.add(message);

        this.revalidate();
        this.repaint();
    }

}
