package worth;

import worth.client.controller.AuthController;
import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;
import worth.utils.UIMessages;
import worth.utils.Utils;

import javax.swing.*;
import java.io.IOException;

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

        // crea model (logica client)
        ClientModel model = null;
        try {
            model = new ClientModel();
        } catch (IOException e) {
            e.printStackTrace();
            int input = Utils.showErrorMessageDialog(UIMessages.CONNECTION_REFUSED);
            if (input == JOptionPane.OK_OPTION || input == JOptionPane.CLOSED_OPTION)
                return;
        }

        AuthUI ui = new AuthUI();
        AuthController authController = new AuthController(model, ui);
    }

}
