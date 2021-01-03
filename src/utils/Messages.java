package worth.utils;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Messaggi di sistema
 */
public class Messages {

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

    // messaggio "caratteri non consentiti"
    public static final String CHARACTERS_NOT_ALLOWED =
            "We're sorry, but this field contains some characters not allowed.\n" +
                    "Valid username should contain only alphanumeric characters, . - and _";

    // messaggio "username non disponibile"
    public static String USERNAME_NOT_AVAILABLE(String username) {
        return "We're sorry, but username '" + username + "' is not available.\n" +
                "Please try with another one :)";
    }

    // messaggio "registrazione riuscita con successo"
    public static String REGISTRATION_SUCCESSFUL = "Registration successful";
}
