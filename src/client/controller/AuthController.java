package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;

import javax.swing.*;

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
            JOptionPane.showMessageDialog (
                    null,
                    response,
                    "Login error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void register() {
        String username = this.view.getUsernameTextField().getText();
        String password = this.view.getPasswordTextField().getText();
        //model.register(username, password); todo implementa
        JOptionPane.showMessageDialog(
                null,
                "reg successfull",
                "Info",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

}
