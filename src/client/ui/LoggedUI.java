package worth.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 04/01/21
 */
public class LoggedUI extends JPanel {
    private static final int WIDTH = 1300; // larghezza del panel
    private static final int HEIGHT = 600; // altezza del panel

    // componenti
    private JPanel buttonsPanel;
    private JPanel containerPanel;
    private JButton myProjectsButton;
    private JButton createProjectButton;
    private JButton logoutButton;

    public LoggedUI() {
        Dimension dim = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(dim);
        this.setLayout(new BorderLayout());

        buttonsPanel = new JPanel();
        containerPanel = new JPanel();

        this.add(buttonsPanel, BorderLayout.NORTH);
        this.add(containerPanel);

        initComponents();
    }

    private void initComponents() {
        myProjectsButton = new JButton("Show my projects");
        createProjectButton = new JButton("Create new project");
        logoutButton = new JButton("Logout");

        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        buttonsPanel.add(myProjectsButton);
        buttonsPanel.add(createProjectButton);
        buttonsPanel.add(logoutButton);
    }

    public JButton getMyProjectsButton() {
        return myProjectsButton;
    }

    public JButton getCreateProjectButton() {
        return createProjectButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

}
