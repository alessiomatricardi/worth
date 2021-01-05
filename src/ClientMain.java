package worth;

import worth.client.controller.AuthController;
import worth.client.controller.LoggedController;
import worth.client.model.ClientModel;
import worth.client.ui.AuthUI;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
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

        // crea pannello di autenticazione dell'utente
        AuthUI authUI = new AuthUI();
        AuthController authController = new AuthController(model, authUI);

        // crea pannello di controllo dell'utente loggato
        LoggedUI loggedUI = new LoggedUI();
        LoggedController loggedController = new LoggedController(model, loggedUI);

        // crea frame
        WorthFrame frame = new WorthFrame(authUI, loggedUI);
    }

}
