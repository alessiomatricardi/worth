package worth.client.ui;

import worth.client.ui.loggedPanels.*;
import worth.client.ui.loggedPanels.ProjectDetailsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Pannello principale di un utente loggato
 */
public class LoggedUI extends JPanel implements HostsCardsContainer {
    private static final int WIDTH = 1000; // larghezza del panel
    private static final int HEIGHT = 600; // altezza del panel
    public static final String HOME_PANEL = "home";
    public static final String USERS_PANEL = "users";
    public static final String PROJECTS_PANEL = "projectsList";
    public static final String PROJECT_DETAILS_PANEL = "projectDetails";

    // componenti
    private final JPanel buttonsPanel;
    private final JPanel containerPanel;

    // buttons
    private final JButton homeButton;
    private final JButton userListButton;
    private final JButton showProjectsListButton;
    private final JButton logoutButton;

    // gestore layout
    private final CardLayout cardLayout;

    // panels gestiti in containerPanel
    private final HomePanel homePanel;
    private final UsersPanel usersPanel;
    private final ProjectsListPanel projectsListPanel;
    private final ProjectDetailsPanel projectDetailsPanel;

    public LoggedUI() {
        Dimension dim = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(dim);
        this.setLayout(new BorderLayout());

        // button panel ha layout FlowLayout
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // aggiungo buttons a buttonsPanel
        homeButton = new JButton("Home");
        userListButton = new JButton("Show users");
        showProjectsListButton = new JButton("Show my projects");
        logoutButton = new JButton("Logout");
        buttonsPanel.add(homeButton);
        buttonsPanel.add(userListButton);
        buttonsPanel.add(showProjectsListButton);
        buttonsPanel.add(logoutButton);

        // containerPanel ha layout CardLayout
        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        // aggiungo panels al container
        homePanel = new HomePanel();
        containerPanel.add(homePanel, HOME_PANEL);
        usersPanel = new UsersPanel();
        containerPanel.add(usersPanel, USERS_PANEL);
        projectsListPanel = new ProjectsListPanel();
        containerPanel.add(projectsListPanel, PROJECTS_PANEL);
        projectDetailsPanel = new ProjectDetailsPanel();
        containerPanel.add(projectDetailsPanel, PROJECT_DETAILS_PANEL);

        // aggiungo pannelli principali
        this.add(buttonsPanel, BorderLayout.NORTH);
        this.add(containerPanel);
    }

    @Override
    public JPanel getContainerPanel() {
        return containerPanel;
    }

    @Override
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public HomePanel getHomePanel() {
        return homePanel;
    }

    public UsersPanel getUsersPanel() {
        return usersPanel;
    }

    public ProjectsListPanel getProjectsListPanel() {
        return projectsListPanel;
    }

    public ProjectDetailsPanel getProjectDetailsPanel() {
        return projectDetailsPanel;
    }

    public JButton getHomeButton() {
        return homeButton;
    }

    public JButton getUserListButton() {
        return userListButton;
    }

    public JButton getShowProjectsListButton() {
        return showProjectsListButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

}
