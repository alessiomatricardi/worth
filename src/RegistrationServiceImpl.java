package worth;

import worth.exceptions.PasswordTooShortException;
import worth.exceptions.CharactersNotAllowedException;
import worth.exceptions.UsernameNotAvailableException;
import worth.utils.PasswordManager;
import worth.utils.PasswordManagerImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Implementazione del servizio RMI di registrazione
 */
public class RegistrationServiceImpl extends UnicastRemoteObject implements RegistrationService {
    private Registration registration;
    private PasswordManager passwordManager;

    public RegistrationServiceImpl(Registration registration) throws RemoteException {
        super();
        this.registration = registration;
        passwordManager = new PasswordManagerImpl();
    }

    @Override
    public synchronized void register (String username, String password)
            throws RemoteException, CharactersNotAllowedException, UsernameNotAvailableException, PasswordTooShortException {
        if (!username.matches(USERNAME_REGEX))
            throw new CharactersNotAllowedException();
        if (password.length() < MIN_PASSWORD_LEN)
            throw new PasswordTooShortException();

        String salt = passwordManager.getSalt();
        String hash = passwordManager.hash(password, salt);
        String passToBeSaved = hash + ":" + salt;

        registration.registerUser(username, passToBeSaved);
    }
}
