package worth.client.ui;

import worth.client.ui.loggedPanels.*;
import worth.client.ui.loggedPanels.ProjectDetailsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Pannello principale di un utente online
 */
public class LoggedUI extends JPanel implements HostsCardsContainer {
    private static final int WIDTH = 1000; // larghezza del panel
    private static final int HEIGHT = 650; // altezza del panel

    // nomi delle card
    public static final String HOME_PANEL = "home";
    public static final String USERS_PANEL = "users";
    public static final String PROJECTS_PANEL = "projectsList";
    public static final String PROJECT_DETAILS_PANEL = "projectDetails";

    // componenti
    private final JPanel buttonsPanel;
    private final JPanel cardContainer;

    // buttons
    private final JButton homeButton;
    private final JButton userListButton;
    private final JButton projectsListButton;
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
        projectsListButton = new JButton("Your projects");
        logoutButton = new JButton("Logout");
        buttonsPanel.add(homeButton);
        buttonsPanel.add(userListButton);
        buttonsPanel.add(projectsListButton);
        buttonsPanel.add(logoutButton);

        // containerPanel ha layout CardLayout
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        // aggiungo panels al container
        homePanel = new HomePanel();
        cardContainer.add(homePanel, HOME_PANEL);
        usersPanel = new UsersPanel();
        cardContainer.add(usersPanel, USERS_PANEL);
        projectsListPanel = new ProjectsListPanel();
        cardContainer.add(projectsListPanel, PROJECTS_PANEL);
        projectDetailsPanel = new ProjectDetailsPanel();
        cardContainer.add(projectDetailsPanel, PROJECT_DETAILS_PANEL);

        // aggiungo pannelli principali
        this.add(buttonsPanel, BorderLayout.NORTH);
        this.add(cardContainer);
    }

    @Override
    public JPanel getCardContainer() {
        return cardContainer;
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

    public JButton getProjectsListButton() {
        return projectsListButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

}
