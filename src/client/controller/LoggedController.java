package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.exceptions.CommunicationException;
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
        this.view.getMyProjectsButton().addActionListener(e -> this.showProjects());
        this.view.getCreateProjectButton().addActionListener(e -> this.createProject());
        this.view.getLogoutButton().addActionListener(e -> this.logout());
    }

    private void showProjects() {

    }

    private void createProject() {

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

    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
