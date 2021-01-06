package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;
import worth.client.ui.WorthFrame;
import worth.exceptions.*;
import worth.utils.UIMessages;
import worth.utils.Utils;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Controller che si occupa della gestione del pannello di accesso
 */
public class AuthController {
    private final ClientModel model;
    private final AuthUI view;

    public AuthController(ClientModel model, AuthUI view) {
        this.model = model;
        this.view = view;
        this.initController();
    }

    private void initController() {
        this.view.getLoginButton().addActionListener(e -> this.login());
        this.view.getRegisterButton().addActionListener(e -> this.register());
    }

    private void login() {
        String username = this.view.getUsernameTextField().getText();
        String password = this.view.getPasswordTextField().getText();
        if (username.isBlank() || password.isBlank()) {
            Utils.showErrorMessageDialog(UIMessages.EMPTY_FIELD);
            return;
        }
        try {
            model.login(username, password);
            this.changeContext();
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        } catch (WrongPasswordException e) {
            Utils.showErrorMessageDialog(UIMessages.PASSWORD_WRONG);
        } catch (AlreadyLoggedException e) {
            Utils.showErrorMessageDialog(UIMessages.USER_ALREADY_LOGGED);
        }
    }

    private void register() {
        String username = this.view.getUsernameTextField().getText();
        String password = this.view.getPasswordTextField().getText();
        if (username.isBlank() || password.isBlank()) {
            Utils.showErrorMessageDialog(UIMessages.EMPTY_FIELD);
            return;
        }
        try {
            model.register(username, password);
            Utils.showInfoMessageDialog(UIMessages.REGISTRATION_SUCCESSFUL);
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (UsernameNotAvailableException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_AVAILABLE(username));
        } catch (PasswordTooShortException e) {
            Utils.showErrorMessageDialog(UIMessages.PASSWORD_TOO_SHORT);
        } catch (CharactersNotAllowedException e) {
            Utils.showErrorMessageDialog(UIMessages.CHARACTERS_NOT_ALLOWED);
        }
    }

    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
