package worth.client.ui.loggedPanels;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by alessiomatricardi on 09/01/21
 *
 * Panel che mostra gli utenti
 */
public class UsersPanel extends JPanel {
    public static final String SHOW_ALL_USERS_TEXT = "Show all users";
    public static final String SHOW_ONLINE_USERS_TEXT = "Show only online users";
    private JButton onlineToggle;       // button per vedere tutti gli utenti o solo quelli online
    private Component userComponent;    // pannello che contiene la lista di utenti

    public UsersPanel() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        onlineToggle = new JButton(SHOW_ONLINE_USERS_TEXT);

        userComponent = null;

        this.add(onlineToggle, BorderLayout.NORTH);
    }

    public void setUI(List<JLabel> users) {
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new GridLayout(0, 1, 0, 0));

        // inserisco gli utenti nel pannello
        for (JLabel user : users) {
            Font font = user.getFont();
            user.setFont(new Font(font.getName(), Font.PLAIN, (int)(font.getSize() * 1.5)));
            user.setPreferredSize(new Dimension(300, 50));
            usersPanel.add(user);
        }

        // container contiene il pannello degli utenti
        JPanel container = new JPanel(new BorderLayout(0,0));
        container.add(usersPanel, BorderLayout.NORTH);

        // layout scrollabile, se necessario
        JScrollPane scrollPane = new JScrollPane(
                container,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        // aumenta velocità dello scorrimento
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // niente bordi
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // elimino pannello precedente e aggiungo il nuovo
        if (this.userComponent != null)
            this.remove(this.userComponent);
        this.userComponent = scrollPane;
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public JButton getOnlineToggle() {
        return onlineToggle;
    }

}
