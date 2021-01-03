package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;
import worth.exceptions.PasswordTooShortException;
import worth.exceptions.CharactersNotAllowedException;
import worth.exceptions.UsernameNotAvailableException;
import worth.utils.Messages;
import worth.utils.Utils;

import javax.swing.*;
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
        } catch (Exception e) {
            String response = e.getMessage();
            response.replaceFirst("^[0-9]+$", "");
            //Utils.showErrorMessageDialog(Messages.) todo
        }
    }

    private void register() {
        String username = this.view.getUsernameTextField().getText();
        String password = this.view.getPasswordTextField().getText();
        try {
            model.register(username, password);
            Utils.showInfoMessageDialog(Messages.REGISTRATION_SUCCESSFUL);
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            Utils.showErrorMessageDialog(Messages.CONNECTION_ERROR);
        } catch (UsernameNotAvailableException e) {
            Utils.showErrorMessageDialog(Messages.USERNAME_NOT_AVAILABLE(username));
        } catch (PasswordTooShortException e) {
            Utils.showErrorMessageDialog(Messages.PASSWORD_TOO_SHORT);
        } catch (CharactersNotAllowedException e) {
            Utils.showErrorMessageDialog(Messages.CHARACTERS_NOT_ALLOWED);
        }
    }

}
