package worth.client.ui.loggedPanels;

import javax.swing.*;
import java.util.Map;

/**
 * Created by alessiomatricardi on 08/01/21
 */
public class ChatPanel extends JPanel {
    // pannelli dove vengono visualizzati i messaggi (uno per chat = uno per progetto)
    // messagePanels.get(nomeProgetto) = pannello chat del progetto
    Map<String, JComponent> messagePanels;
}
