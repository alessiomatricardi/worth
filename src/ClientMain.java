import client.ui.AuthUI;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class ClientMain {

    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(RegistrationTask.REGISTRY_PORT);

        RegistrationService service = (RegistrationService) registry.lookup(RegistrationService.REGISTRATION_SERVICE_NAME);

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        AuthUI ui = new AuthUI();

        /*try {
            service.register("prova", "prova");
            System.out.println("ok");
        } catch (SpacesNotAllowedException e) {
            e.printStackTrace();
        } catch (UsernameNotAvailableException e) {
            e.printStackTrace();
        } catch (PasswordTooShortException e) {
            e.printStackTrace();
        }
        try {
            service.register("pr ova", "provaffffff");
            System.out.println("ok");
        } catch (SpacesNotAllowedException e) {
            e.printStackTrace();
        } catch (UsernameNotAvailableException e) {
            e.printStackTrace();
        } catch (PasswordTooShortException e) {
            e.printStackTrace();
        }
        try {
            service.register("prova", "provafffffff");
            System.out.println("ok");
        } catch (SpacesNotAllowedException e) {
            e.printStackTrace();
        } catch (UsernameNotAvailableException e) {
            e.printStackTrace();
        } catch (PasswordTooShortException e) {
            e.printStackTrace();
        }
        try {
            service.register("prova", "provahhhhhhhhh");
            System.out.println("ok");
        } catch (SpacesNotAllowedException e) {
            e.printStackTrace();
        } catch (UsernameNotAvailableException e) {
            e.printStackTrace();
        } catch (PasswordTooShortException e) {
            e.printStackTrace();
        }*/
    }

}
