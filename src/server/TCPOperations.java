package worth.server;

import worth.data.*;
import worth.exceptions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Interfaccia che dichiara tutte le operazioni TCP che il server dovrà assolvere
 */
public interface TCPOperations { // todo interface

    /**
     * Effettua il login
     *
     * @param username username dell'utente di cui fare il login
     * @param password password dell'utente
     *
     * @throws UserNotExistsException se l'utente non esiste
     * @throws AlreadyLoggedException se l'utente è già loggato al servizio
     * @throws WrongPasswordException se la password immessa è errata
     */
    void login(String username, String password)
            throws UserNotExistsException, AlreadyLoggedException, WrongPasswordException;

    /**
     * Effettua il logout
     *
     * @param username username dell'utente di cui fare il logout
     *
     * @throws UserNotExistsException se l'utente non esiste
     */
    void logout(String username) throws UserNotExistsException;

    /**
     * Mostra la lista dei progetti di cui l'utente fa parte
     *
     * @param username utente
     *
     * @throws UserNotExistsException se l'utente non esiste
     *
     * @return lista dei progetti dell'utente
     */
    List<Project> listProjects(String username) throws UserNotExistsException;

    /**
     * Crea un nuovo progetto con nome projectName
     *
     * @param projectName nome del progetto
     *
     * @throws ProjectAlreadyExistsException se un progetto con quel nome esiste già
     * @throws NoSuchAddressException se non ci sono più indirizzi multicast disponibili
     * @throws IOException se ci sono errori nel salvataggio del progetto
     *
     */
    void createProject(String projectName) throws ProjectAlreadyExistsException, NoSuchAddressException, IOException;

    /**
     * Aggiunge un membro al progetto
     *
     * @param projectName nome del progetto
     * @param username utente da aggiungere al progetto
     * @param whoRequest utente che richiede l'operazione
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     * @throws UserAlreadyPresentException se l'utente fa già parte del progetto
     * @throws UserNotExistsException se l'utente non esiste
     *
     */
    void addMember(String projectName, String username, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, UserAlreadyPresentException, UserNotExistsException;

    /**
     * Mostra i membri del progetto
     *
     * @param projectName nome del progetto
     * @param whoRequest utente che richiede l'operazione
     *
     * @return lista dei membri del progetto
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     *
     */
    List<String> showMembers(String projectName, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException;

    /**
     * Mostra le card del progetto
     *
     * @param projectName nome del progetto
     * @param whoRequest utente che richiede l'operazione
     *
     * @return lista delle cards del progetto
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     *
     */
    List<Card> showCards(String projectName, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException;

    /**
     * Mostra una card specifica del progetto
     *
     * @param projectName nome del progetto
     * @param cardName nome della card
     * @param whoRequest utente che richiede l'operazione
     *
     * @return card del progetto con nome cardName
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     * @throws CardNotExistsException se la card non esiste
     *
     */
    Card showCard(String projectName, String cardName, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, CardNotExistsException;

    /**
     * Aggiungi una card al progetto
     *
     * @param projectName nome del progetto
     * @param cardName nome della card
     * @param description descrizione associata alla card
     * @param whoRequest utente che richiede l'operazione
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     * @throws CardAlreadyExistsException se la card esiste già nel progetto
     *
     */
    void addCard(String projectName, String cardName, String description, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, CardAlreadyExistsException, IOException;

    /**
     * Sposta una card del progetto dallo stato from allo stato to
     *
     * @param projectName nome del progetto
     * @param cardName nome della card
     * @param from stato di partenza
     * @param to stato di destinazione
     * @param whoRequest utente che richiede l'operazione
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     * @throws CardNotExistsException se la card non esiste
     * @throws OperationNotAllowedException
     * se la card non è nello stato from oppure lo spostamento from->to non è consentito
     *
     */
    void moveCard(String projectName, String cardName, CardStatus from, CardStatus to, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, CardNotExistsException, OperationNotAllowedException, IOException;

    /**
     * Sposta una card del progetto
     *
     * @param projectName nome del progetto
     * @param cardName nome della card
     * @param whoRequest utente che richiede l'operazione
     *
     * @return lista dei movimenti della card
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     * @throws CardNotExistsException se la card non esiste
     *
     */
    List<Movement> getCardHistory(String projectName, String cardName, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, CardNotExistsException;

    /**
     * Ottieni l'indirizzo multicast della chat del progetto
     *
     * @param projectName nome del progetto
     * @param whoRequest utente che richiede l'operazione
     *
     * @return indirizzo multicast della chat del progetto
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     *
     */
    String readChat(String projectName, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException;

    /**
     * Cancella progetto
     *
     * @param projectName nome del progetto
     * @param whoRequest utente che richiede l'operazione
     *
     * @throws ProjectNotExistsException se il progetto non esiste
     * @throws UnauthorizedUserException se non si hanno le autorizzazioni necessarie
     * @throws ProjectNotCloseableException se non tutte le card sono nello stato DONE
     *
     */
    void cancelProject(String projectName, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, ProjectNotCloseableException;

    /**
     * Ottieni lo stato di tutti gli utenti
     *
     * @return mappa di utenti ed il loro stato (online, offline)
     *
     */
    Map<String, UserStatus> getUserStatus();
}
