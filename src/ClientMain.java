package worth;

import worth.client.controller.AuthController;
import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;

import javax.swing.*;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class ClientMain {

    public static void main(String[] args) {

        // setta UI di sistema
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        ClientModel model = null;
        try {
            model = new ClientModel();
        } catch (IOException e) {
            e.printStackTrace();
            // non posso fare nulla
            // todo joptionpane??
            return;
        }

        AuthUI ui = new AuthUI();
        AuthController authController = new AuthController(model, ui);
    }

}
