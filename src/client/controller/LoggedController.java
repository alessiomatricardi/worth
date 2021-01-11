package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.HostsCardsContainer;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.client.ui.loggedPanels.*;
import worth.client.ui.loggedPanels.projectPanels.ChatMessageList;
import worth.client.ui.loggedPanels.ProjectDetailsPanel;
import worth.data.Project;
import worth.data.UserStatus;
import worth.exceptions.*;
import worth.protocol.CommunicationProtocol;
import worth.utils.UIMessages;
import worth.utils.Utils;

import javax.swing.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Controller che gestisce la UI dell'utente loggato
 */
public class LoggedController {
    private final ClientModel model;
    private final LoggedUI view;

    public LoggedController(ClientModel model, LoggedUI view) {
        this.model = model;
        this.view = view;
        this.initController();
    }

    private void initController() {
        // azioni possibili in LoggedUI
        this.view.getHomeButton().addActionListener(e -> this.showHome());

        this.view.getUserListButton().addActionListener(e -> this.showUsers(false));

        this.view.getShowProjectsListButton().addActionListener(e -> this.showProjectsList());

        this.view.getLogoutButton().addActionListener(e -> {
            int result = Utils.showQuestionMessageDialog(UIMessages.LOGOUT_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                this.logout();
            }
        });

        // azioni possibili in panel contenuti dentro la view

        // dentro HomePanel posso creare un progetto
        HomePanel homePanel = this.view.getHomePanel();
        homePanel.getCreateProjectButton().addActionListener(e -> this.createProject());

        // dentro UsersPanel posso vedere la lista degli utenti
        UsersPanel usersPanel = this.view.getUsersPanel();
        usersPanel.getOnlineToggle().addActionListener(e -> {
            if (usersPanel.getOnlineToggle().getText().equals(UsersPanel.SHOW_ALL_USERS_TEXT)) {
                // voglio vedere tutti gli utenti
                this.showUsers(false);
                usersPanel.getOnlineToggle().setText(UsersPanel.SHOW_ONLY_ON_TEXT);
            } else {
                // voglio vedere solo gli utenti online
                this.showUsers(true);
                usersPanel.getOnlineToggle().setText(UsersPanel.SHOW_ALL_USERS_TEXT);
            }
        });

        // dentro ProjectsListPanel posso vedere la lista dei progetti dell'utente online
        ProjectsListPanel projectsListPanel = this.view.getProjectsListPanel();

        // dentro ProjectsDetailsPanel posso vedere i dettagli di un singolo progetto
        ProjectDetailsPanel projectDetailsPanel = this.view.getProjectDetailsPanel();

    }

    // visualizza cards dentro LoggedUI

    private void showHome() {
        this.showCard(this.view, LoggedUI.HOME_PANEL);
    }

    private void showUsers(boolean onlyOnlineUsers) {
        List<JLabel> labels = new ArrayList<>();
        if (onlyOnlineUsers) {
            List<String> users = this.model.listOnlineUsers();
            for (String user : users) {
                String text = user + " : ONLINE";
                labels.add(new JLabel(text));
            }
        } else {
            Map<String, UserStatus> userStatus= this.model.listUsers();
            Set<String> users = userStatus.keySet();
            for (String user : users) {
                String text = user + " : " + userStatus.get(user).name();
                labels.add(new JLabel(text));
            }
        }
        UsersPanel usersPanel = this.view.getUsersPanel();
        usersPanel.setUI(labels);
        // aggiorno UI
        this.updateUI(usersPanel);
        this.showCard(this.view, LoggedUI.USERS_PANEL);
    }

    private void showProjectsList() {
        try {
            List<Project> projects = this.model.listProjects();
            // creo buttons
            List<JButton> buttons = new ArrayList<>();
            for (Project project : projects) {
                // creo bottone con nome del progetto
                JButton button = new JButton(project.getName());
                // aggiungo azione
                // quando clicco sul bottone, vado ai dettagli del progetto
                button.addActionListener(e -> this.showProjectDetails(button.getText()));
                // aggiungo alla lista
                buttons.add(button);
            }
            // aggiungo elementi
            ProjectsListPanel projectsListPanel = this.view.getProjectsListPanel();
            projectsListPanel.setUI(buttons);
            // aggiorno UI
            this.updateUI(projectsListPanel);
            this.showCard(this.view, LoggedUI.PROJECTS_PANEL);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        }
    }

    private void showProjectDetails(String projectName) {
        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();

        // todo controllers

        detailsPanel.setUI(projectName);
        this.showCard(this.view, LoggedUI.PROJECT_DETAILS_PANEL);
    }

    // visualizza cards dentro ProjectDetails todo

    private void showProjectCards() {

    }

    private void showProjectMembers() {

    }

    private void showCardDetails() {

    }

    private void showProjectChat() {

    }

    private void readChat() {
        // seleziona nome progetto todo
        try {
            String chatAddress = this.model.readChat("a");
            if (chatAddress != null) {
                ChatMessageList chatMessageList = new ChatMessageList();
                ChatReaderControllerTask chatReaderControllerTask = new ChatReaderControllerTask(
                        this.model.getUsername(),
                        this.model.getMulticastSocket(),
                        chatAddress,
                        CommunicationProtocol.UDP_CHAT_PORT,
                        chatMessageList
                );
                ExecutorService threadPool = this.model.getThreadPool();
                threadPool.execute(chatReaderControllerTask);
            }
            // visualizza quella card todo
        } catch (UnknownHostException | CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        }
    }

    private void createProject() {
        HomePanel homePanel = this.view.getHomePanel();
        String projectName = homePanel.getProjectNameField().getText();
        if (projectName.isBlank()) {
            Utils.showErrorMessageDialog(UIMessages.EMPTY_FIELD);
            return;
        }
        try {
            this.model.createProject(projectName);
            Utils.showInfoMessageDialog(UIMessages.PROJECT_SUCCESS);
            // resetto campo
            homePanel.getProjectNameField().setText("");
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (NoSuchAddressException e) {
            Utils.showErrorMessageDialog(UIMessages.NO_SUCH_ADDRESS);
        } catch (ProjectAlreadyExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_ALREADY_EXISTS);
        } catch (CharactersNotAllowedException e) {
            Utils.showErrorMessageDialog(UIMessages.CHARACTERS_NOT_ALLOWED);
        }
    }

    private void logout() {
        try {
            this.model.logout();
            this.changeContext();
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        }
    }

    private void updateUI(JPanel panel) {
        panel.revalidate();
        panel.repaint();
    }

    // visualizza la card cardName all'interno del panel hostPanel
    private void showCard(HostsCardsContainer hostPanel, String cardName) {
        hostPanel.getCardLayout().show(this.view.getContainerPanel(), cardName);
    }

    // torna alla schermata di login
    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
