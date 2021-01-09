package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.client.ui.loggedPanels.HomePanel;
import worth.client.ui.loggedPanels.ProjectDetailPanel;
import worth.client.ui.loggedPanels.ShowProjectsPanel;
import worth.exceptions.*;
import worth.utils.UIMessages;
import worth.utils.Utils;

import javax.swing.*;

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

        this.view.getUserListButton().addActionListener(e -> this.showUsers());

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

        ShowProjectsPanel showProjectsPanel = this.view.getShowProjectsPanel();

        ProjectDetailPanel projectDetailPanel = this.view.getProjectDetailPanel();

    }

    private void showHome() {
        String username = this.model.getUsername();
        this.view.getHomePanel().setUsernameLabel(username);
        this.updateUI(this.view.getHomePanel());
        this.showPanel(LoggedUI.HOME_PANEL);
    }

    private void showUsers() {
        // recupera utenti todo
        this.showPanel(LoggedUI.USERS_PANEL);
    }

    private void showProjects() {
        // recupera progetti todo
        this.showPanel(LoggedUI.PROJECTS_PANEL);
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
