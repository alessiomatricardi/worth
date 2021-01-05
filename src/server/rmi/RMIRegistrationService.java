package worth.server.rmi;

import worth.exceptions.PasswordTooShortException;
import worth.exceptions.CharactersNotAllowedException;
import worth.exceptions.UsernameNotAvailableException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Interfaccia del servizio RMI di registrazione
 */
public interface RMIRegistrationService extends Remote {

    /**
     * Registrazione di utente su WORTH
     *
     * @param username username dell'utente
     * @param password password dell'utente
     *
     * @throws RemoteException se ci sono problemi legati alla connessione
     * @throws CharactersNotAllowedException se l'username contiene spazi
     * @throws UsernameNotAvailableException se l'username non è disponibile
     * @throws PasswordTooShortException se la dimensione della password è più corta di MIN_PASS_LEN
     */
    void register(String username, String password)
            throws RemoteException, CharactersNotAllowedException, UsernameNotAvailableException, PasswordTooShortException;
}
