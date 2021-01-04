package worth;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Insieme di tutti i dettagli di comunicazione client-server del servizio Worth
 */
public abstract class CommunicationProtocol {

    // Porta del registry per recupero servizio di registrazione
    public static final int REGISTRY_PORT = 6789;

    // Nome del servizio di registrazione offerto da RMI
    public static final String REGISTRATION_SERVICE_NAME = "RegistrationService";

    // Lunghezza minima della password
    public static final int MIN_PASSWORD_LEN = 8;

    // REGEX username, solo a-z, A-Z, 0-9, . - _ consentiti
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9._-]+$";

    // IP del server per richieste TCP
    public static final String SERVER_IP_ADDRESS = "localhost";

    // Porta del server per richieste TCP
    public static final int SERVER_PORT = 2500;

    // carattere separatore
    public static final String SEPARATOR = " ";

    public static final String LOGIN_CMD = "login";
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_USERNOTEXISTS = 1;
    public static final int LOGIN_WRONGPWD = 2;
    public static final int LOGIN_COMMUNICATION_ERROR = 3;

    public static final String LOGOUT_CMD = "logout";

    public static final String CREATEPROJECT_CMD = "create_project";

    public static final String ADD_CARD_CMD = "add_card";

    public static final String MOVE_CARD_CMD = "move_card";

    public static final String ADD_MEMBER_CMD = "add_member";

    public static final String SHOW_CARDS_CMD = "show_cards";

    public static final String CANCELPROJECT_CMD = "cancel_project";

}
