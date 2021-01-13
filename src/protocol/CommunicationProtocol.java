package worth.protocol;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Insieme di tutti i dettagli di comunicazione client-server del servizio Worth
 */
public abstract class CommunicationProtocol {
    // IP del server per richieste TCP
    public static final String SERVER_IP_ADDRESS = "localhost";

    // Porta del server per richieste TCP
    public static final int SERVER_PORT = 2500;

    // Porta del registry per recupero servizio di registrazione
    public static final int REGISTRY_PORT = 6789;

    // Porta utilizzata nelle chat multicast per inviare e ricevere messaggi
    public static final int UDP_CHAT_PORT = 50000;

    // quantità massima di bytes trasportabili in un messaggio UDP
    // 16 bit -> 2^16 = 65536
    public static final int UDP_MSG_MAX_LEN = 65536;

    // nome del sistema (utilizzato principalmente come autore dei messaggi UDP)
    public final static String SYSTEM_NAME = "System";

    // messaggio che, se inviato dal server, chiude il servizio di chat
    public final static String UDP_TERMINATE_MSG = "stop";

    // Nome del servizio di registrazione offerto da RMI
    public static final String REGISTRATION_SERVICE_NAME =
            "rmi://" + SERVER_IP_ADDRESS +":" + REGISTRY_PORT + "/RegistrationService";

    // Nome del servizio di callback offerto da RMI
    public static final String CALLBACK_SERVICE_NAME =
            "rmi://" + SERVER_IP_ADDRESS +":" + REGISTRY_PORT + "/CallbackService";

    // Lunghezza minima della password
    public static final int MIN_PASSWORD_LEN = 8;

    // REGEX username, solo a-z, 0-9, _ consentiti
    public static final String STRING_REGEX = "^[a-z0-9_]+$";

    /**
    * COMANDI E CODICI DI RISPOSTA DURANTE LA COMUNICAZIONE TCP
    */

    // codice non identificato, utilizzato come valore di base prima di calcolare il vero codice
    public static final int UNKNOWN = -1;

    // operazione avvenuta con successo
    public static final int OP_SUCCESS = 0;

    // codice che identifica un errore nei messaggi di risposta
    public static final int COMMUNICATION_ERROR = 100;

    // l'utente non esiste
    public static final int USER_NOT_EXISTS = 101;

    // il progetto non esiste
    public static final int PROJECT_NOT_EXISTS = 102;

    // la card non esiste
    public static final int CARD_NOT_EXISTS = 103;

    // utente non autorizzato
    public static final int UNAUTHORIZED = 104;

    // caratteri non consentiti
    public static final int CHARS_NOT_ALLOWED = 105;


    public static final String LOGIN_CMD = "login";
    public static final int LOGIN_WRONGPWD = 1;
    public static final int LOGIN_ALREADY_LOGGED = 2;

    public static final String LOGOUT_CMD = "logout";

    public static final String LISTPROJECTS_CMD = "list_projects";

    public static final String CREATEPROJECT_CMD = "create_project";
    public static final int CREATEPROJECT_ALREADYEXISTS = 1;
    public static final int CREATEPROJECT_NOMOREADDRESSES = 2;

    public static final String ADD_MEMBER_CMD = "add_member";
    public static final int ADD_MEMBER_ALREADYPRESENT = 1;

    public static final String SHOW_MEMBERS_CMD = "show_members";

    public static final String SHOW_CARDS_CMD = "show_cards";

    public static final String SHOW_CARD_CMD = "show_card";

    public static final String ADD_CARD_CMD = "add_card";
    public static final int ADD_CARD_ALREADYEXISTS = 1;

    public static final String MOVE_CARD_CMD = "move_card";
    public static final int MOVE_CARD_NOT_ALLOWED = 1;

    public static final String CARD_HISTORY_CMD = "card_history";

    public static final String READ_CHAT_CMD = "read_chat";

    public static final String CANCELPROJECT_CMD = "cancel_project";
    public static final int CANCELPROJECT_NOTCANCELABLE = 1;

}
