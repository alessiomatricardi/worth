package worth.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by alessiomatricardi on 08/01/21
 *
 * Struttura di un messaggio UDP inviato nella chat
 */
public class UDPMessage {
    // messaggio che, inviato dal server, chiude il servizio di chat
    public final static String TERMINATE = "stop";

    private String author;          // nome di colui che invia il messaggio
    private String message;         // messaggio da inviare
    private boolean isFromSystem;   // è il sistema a inviare il messaggio?

    public UDPMessage(String author, String message, boolean isFromSystem) {
        this.author = author;
        this.message = message;
        this.isFromSystem = isFromSystem;
    }

    @JsonCreator
    private UDPMessage() {}

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromSystem() {
        return isFromSystem;
    }
}
