package worth.server.rmi;

import worth.data.UserStatus;
import worth.protocol.CommunicationProtocol;
import worth.exceptions.PasswordTooShortException;
import worth.exceptions.CharactersNotAllowedException;
import worth.exceptions.UsernameNotAvailableException;
import worth.server.Registration;
import worth.utils.PasswordManager;
import worth.utils.PasswordManagerImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Implementazione del servizio RMI di registrazione
 */
public class RMIRegistrationServiceImpl extends UnicastRemoteObject implements RMIRegistrationService {
    private Registration registration;
    private RMICallbackServiceImpl callbackService;
    private PasswordManager passwordManager;

    public RMIRegistrationServiceImpl(Registration registration, RMICallbackServiceImpl callbackService) throws RemoteException {
        super();
        this.callbackService = callbackService;
        this.registration = registration;
        passwordManager = new PasswordManagerImpl();
    }

    @Override
    public synchronized void register (String username, String password)
            throws RemoteException, CharactersNotAllowedException, UsernameNotAvailableException, PasswordTooShortException {
        if (!username.matches(CommunicationProtocol.USERNAME_REGEX))
            throw new CharactersNotAllowedException();
        if (password.length() < CommunicationProtocol.MIN_PASSWORD_LEN)
            throw new PasswordTooShortException();

        String salt = passwordManager.getSalt();
        String hash = passwordManager.hash(password, salt);

        registration.registerUser(username, hash, salt);

        // notifica gli utenti che l'utente 'username' si è registrato
        callbackService.notifyUsers(username, UserStatus.OFFLINE);
    }
}
