package worth.utils;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Messaggi di sistema
 */
public class UIMessages {

    // messaggio "errore generico"
    public static final String GENERIC_ERROR = "Sorry, we encountered an error.\n" +
            "If the problem persists, please close and restart the application";

    // messaggio "errore di connessione"
    public static final String CONNECTION_ERROR = "Sorry, we encountered some connection problems.\n" +
            "If the problem persists, please close and restart the application";

    // messaggio "errore di connessione rifiutata"
    public static final String CONNECTION_REFUSED = "Sorry, but the connection seems to be refused by the server";

    // messaggio "password troppo corta"
    public static final String PASSWORD_TOO_SHORT =
            "The entered password is too short.\n" +
            "For your security, insert a password with at least 8 characters";

    // messaggio "password sbagliata"
    public static final String PASSWORD_WRONG = "Entered password is wrong";

    // messaggio "caratteri non consentiti"
    public static final String CHARACTERS_NOT_ALLOWED =
            "We're sorry, but this field contains some not allowed characters.\n" +
                    "It should contain only lower alphanumeric characters and _";

    //
    public static final String EMPTY_FIELD = "Some fields seems to be empty or blank.\n";

    // messaggio "username non disponibile"
    public static String USERNAME_NOT_AVAILABLE(String username) {
        return "We're sorry, but username '" + username + "' is not available.\n" +
                "Please try with another one :)";
    }

    // messaggio "username inesistente"
    public static final String USERNAME_NOT_EXISTS = "This username does not exists";

    // messaggio "username già online"
    public static final String USER_ALREADY_LOGGED = "Sorry, but seems that you're already logged in" +
            " from another terminal or application window.";

    // messaggio "registrazione riuscita con successo"
    public static final String REGISTRATION_SUCCESSFUL = "Registration successful";

    // messaggio "card inesistente"
    public static final String CARD_NOT_EXISTS = "This card not exists.";

    // messaggio "operazione non permessa"
    public static final String OPERATION_NOT_ALLOWED = "Sorry, but seems that you're trying" +
            " to do an illegal operation.";

    // messaggio "card già presente nel progetto"
    public static final String CARD_ALREADY_EXISTS = "This card can't be added because " +
            "it already exists in the project.";

    // messaggio "indirizzi finiti"
    public static final String NO_SUCH_ADDRESS = "Sorry, but the project can't be created because" +
            " we passed the maximum number of chat addresses.";

    // messaggio "porte finite"
    public static final String NO_SUCH_PORT = "Sorry, but the project can't be created because" +
            " we passed the maximum number of ports.";

    // messaggio "progetto già esistente"
    public static final String PROJECT_ALREADY_EXISTS = "The project can't be created because" +
            " already exists another project with this name.";

    // messaggio "progetto non cancellabile"
    public static final String PROJECT_NOT_CANCELABLE = "The project can't be cancelled because" +
            " there are some tasks to do yet.";

    // messaggio "progetto inesistente"
    public static final String PROJECT_NOT_EXISTS = "You're trying to access" +
            " to a not existing project. Maybe it has been canceled while" +
            " you were trying to modify it.";

    // messaggio "utente non autorizzato"
    public static final String UNAUTHORIZED_USER = "It seems that you are not authorized to access this data.";

    // messaggio "utente già presente nel progetto"
    public static final String USER_ALREADY_PRESENT = "The user is already in this project.";

    // messaggio "sei sicuro di voler fare il logout"
    public static final String LOGOUT_MESSAGE = "Do you want to log out from Worth?";

    // messaggio "progetto creato con successo"
    public static final String PROJECT_CREATE_SUCCESS = "Project successfully created.";

    // messaggio "progetto creato con successo"
    public static final String PROJECT_CANCEL_SUCCESS = "Project successfully cancelled.";

    // messaggio "membro aggiunto con successo"
    public static final String ADD_MEMBER_SUCCESS = "Member successfully added.";

    // messaggio "indirizzo multicast non ottenibile"
    public static final String UNOBTAINABLE_ADDRESS = "Sorry, but we can't send this message.\n" +
            "Seems like there's no saved chat address.\n" +
            "Try again, if the problem persists, restart the application";

    // messaggio "datagramma troppo grande"
    public static final String DATAGRAM_TOO_BIG = "The message you're trying to send is too big.";

}
