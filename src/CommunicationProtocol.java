package worth;

/**
 * Created by alessiomatricardi on 03/01/21
 *
 * Insieme di tutti i dettagli di comunicazione client-server del servizio Worth
 */
public class CommunicationProtocol {

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
}
