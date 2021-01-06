package worth.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 04/01/21
 */
public class WorthFrame extends JFrame {
    private final static String TITLE = "Worth";
    private final static String AUTH_UI = "AuthUI";
    private final static String LOGGED_UI = "LoggedUI";

    private final CardLayout cardLayout;
    private final JPanel cardHolder;

    public WorthFrame(AuthUI authPanel, LoggedUI loggedPanel) {
        super(TITLE);

        this.cardLayout = new MyCardLayout();
        this.cardHolder = new JPanel(cardLayout);

        JPanel authContainerPanel = new JPanel(new GridBagLayout());
        authContainerPanel.add(authPanel);
        this.cardHolder.add(authContainerPanel, AUTH_UI);
        JPanel loggedContainerPanel = new JPanel(new GridBagLayout());
        loggedContainerPanel.add(loggedPanel);
        this.cardHolder.add(loggedContainerPanel, LOGGED_UI);

        this.getContentPane().add(cardHolder);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.setVisible(true);
    }

    public CardLayout getCardLayout() {
        return this.cardLayout;
    }

    public JPanel getCardHolder() {
        return this.cardHolder;
    }

}
