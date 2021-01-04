package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;
import worth.exceptions.*;
import worth.utils.UIMessages;
import worth.utils.Utils;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 03/01/21
 */
public class AuthController {
    private ClientModel model;
    private AuthUI view;

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
        try {
            model.login(username, password);
            Utils.showInfoMessageDialog("ok"); // todo rimuovi
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        } catch (WrongPasswordException e) {
            Utils.showErrorMessageDialog(UIMessages.PASSWORD_WRONG);
        }
    }

    private void register() {
        String username = this.view.getUsernameTextField().getText();
        String password = this.view.getPasswordTextField().getText();
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

}
