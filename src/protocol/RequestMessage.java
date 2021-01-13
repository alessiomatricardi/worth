package worth.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * Created by alessiomatricardi on 13/01/21
 *
 * Identifica un messaggio di richiesta che un client invia al server
 */
public class RequestMessage implements Serializable {
    private String command;
    private String[] arguments;

    public RequestMessage(String command, String... arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    @JsonCreator
    private RequestMessage() {}

    public String getCommand() {
        return this.command;
    }

    public String[] getArguments() {
        return this.arguments;
    }
}
