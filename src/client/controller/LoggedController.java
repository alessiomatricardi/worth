package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.client.ui.loggedPanels.HomePanel;
import worth.client.ui.loggedPanels.ProjectDetailPanel;
import worth.client.ui.loggedPanels.ShowProjectsPanel;
import worth.exceptions.CommunicationException;
import worth.exceptions.NoSuchAddressException;
import worth.exceptions.ProjectAlreadyExistsException;
import worth.exceptions.UserNotExistsException;
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
        this.view.getHomeButton().addActionListener(e -> {
            this.showHome();
        });
        this.view.getUserListButton().addActionListener(e -> {
            // recupera utenti todo
            this.showUsers();

        });
        this.view.getShowProjectsButton().addActionListener(e -> {
            // recupera progetti todo
            this.showProjects();

        });
        this.view.getLogoutButton().addActionListener(e -> {
            int result = Utils.showQuestionMessageDialog(UIMessages.LOGOUT_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                this.logout();
            }
        });

        // azioni possibili in panel contenuti dentro la vista
        HomePanel homePanel = this.view.getHomePanel();

        ShowProjectsPanel showProjectsPanel = this.view.getShowProjectsPanel();

        ProjectDetailPanel projectDetailPanel = this.view.getProjectDetailPanel();

    }

    private void showHome() {
        String username = this.model.getUsername();
        this.view.getHomePanel().setUsernameLabel(username);
        this.view.getHomePanel().revalidate();
        this.view.getHomePanel().repaint();
        this.showPanel(LoggedUI.HOME_PANEL);
    }

    private void showUsers() {
        this.showPanel(LoggedUI.USERS_PANEL);
    }

    private void showProjects() {
        this.showPanel(LoggedUI.PROJECTS_PANEL);
    }

    private void createProject() {
        // prendi dati da casella
        try {
            this.model.createProject("prova444"); // prova todo togli
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (NoSuchAddressException e) {
            Utils.showErrorMessageDialog(UIMessages.NO_SUCH_ADDRESS);
        } catch (ProjectAlreadyExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_ALREADY_EXISTS);
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

    private void showPanel(String panelName) {
        this.view.getCardLayout().show(this.view.getContainerPanel(), panelName);
    }

    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
