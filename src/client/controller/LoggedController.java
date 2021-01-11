package worth.client.controller;

import worth.client.ReadChatTask;
import worth.client.model.ClientModel;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.client.ui.loggedPanels.*;
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

        this.view.getShowProjectsButton().addActionListener(e -> this.showProjects());

        this.view.getLogoutButton().addActionListener(e -> {
            int result = Utils.showQuestionMessageDialog(UIMessages.LOGOUT_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                this.logout();
            }
        });

        // azioni possibili in panel contenuti dentro la view
        HomePanel homePanel = this.view.getHomePanel();
        homePanel.getCreateProjectButton().addActionListener(e -> this.createProject());

        ShowUsersPanel showUsersPanel = this.view.getShowUsersPanel();
        showUsersPanel.getOnlineToggle().addActionListener(e -> {
            if (showUsersPanel.getOnlineToggle().getText().equals(ShowUsersPanel.SHOW_ALL_USERS_TEXT)) {
                this.showUsers(false);
                showUsersPanel.getOnlineToggle().setText(ShowUsersPanel.SHOW_ONLY_ON_TEXT);
            } else {
                this.showUsers(true);
                showUsersPanel.getOnlineToggle().setText(ShowUsersPanel.SHOW_ALL_USERS_TEXT);
            }
        });

        ShowProjectsPanel showProjectsPanel = this.view.getShowProjectsPanel();

        ProjectDetailPanel projectDetailPanel = this.view.getProjectDetailPanel();

    }

    private void showHome() {
        this.showPanel(LoggedUI.HOME_PANEL);
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
        ShowUsersPanel showUsersPanel = this.view.getShowUsersPanel();
        showUsersPanel.setUI(labels);
        // aggiorno UI
        this.updateUI(showUsersPanel);
        this.showPanel(LoggedUI.USERS_PANEL);
    }

    private void showProjects() {
        try {
            List<Project> projects = this.model.listProjects();
            // creo buttons
            List<JButton> buttons = new ArrayList<>();
            for (Project project : projects) {
                JButton button = new JButton(project.getName());
                // aggiungo azione
                button.addActionListener(e -> this.showProjectDetails(button.getText()));
                // aggiungo alla lista
                buttons.add(button);
            }
            // aggiungo elementi
            ShowProjectsPanel showProjectsPanel = this.view.getShowProjectsPanel();
            showProjectsPanel.setUI(buttons);
            // aggiorno UI
            this.updateUI(showProjectsPanel);
            this.showPanel(LoggedUI.PROJECTS_PANEL);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        }
    }

    private void showProjectDetails(String projectName) {
        // todo
    }

    private void readChat() {
        // seleziona nome progetto todo
        try {
            String chatAddress = this.model.readChat("a");
            if (chatAddress != null) {
                ChatMessageList chatMessageList = new ChatMessageList();
                ReadChatTask readChatTask = new ReadChatTask(
                        this.model.getUsername(),
                        this.model.getMulticastSocket(),
                        chatAddress,
                        CommunicationProtocol.UDP_CHAT_PORT,
                        chatMessageList
                );
                ExecutorService threadPool = this.model.getThreadPool();
                threadPool.execute(readChatTask);
            }
            // visualizza quella card
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

    // visualizza un pannello todo completa
    private void showPanel(String panelName) {
        this.view.getCardLayout().show(this.view.getContainerPanel(), panelName);
    }

    // torna alla schermata di login
    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
