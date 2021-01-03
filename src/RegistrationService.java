import exceptions.PasswordTooShortException;
import exceptions.SpacesNotAllowedException;
import exceptions.UsernameNotAvailableException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Interfaccia del servizio RMI di registrazione
 */
public interface RegistrationService extends Remote {
    String REGISTRATION_SERVICE_NAME = "RegistrationService";
    int MIN_PASSWORD_LEN = 8;

    /**
     * Registrazione di utente su WORTH
     *
     * @param username username dell'utente
     * @param password password dell'utente
     *
     * @throws SpacesNotAllowedException se l'username contiene spazi
     * @throws UsernameNotAvailableException se l'username non è disponibile
     * @throws PasswordTooShortException se la dimensione della password è più corta di MIN_PASS_LEN
     */
    void register(String username, String password)
            throws RemoteException, SpacesNotAllowedException, UsernameNotAvailableException, PasswordTooShortException;
}
