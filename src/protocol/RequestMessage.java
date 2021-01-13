package worth.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alessiomatricardi on 13/01/21
 *
 * Identifica un messaggio di richiesta che un client invia al server
 */
public class RequestMessage implements Serializable {
    private String command;
    private List<String> arguments;

    public RequestMessage(String command, String... arguments) {
        this.command = command;
        this.arguments = Arrays.asList(arguments);
    }

    @JsonCreator
    private RequestMessage() {}

    public String getCommand() {
        return this.command;
    }

    public List<String> getArguments() {
        return this.arguments;
    }
}
