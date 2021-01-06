package worth.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Identifica un messaggio di risposta che il server invia al client
 */
public class ResponseMessage implements Serializable {
    private int statusCode; // status code risultante dalla operazione
    private String responseBody; // corpo del messaggio (può essere null)

    public ResponseMessage(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    @JsonCreator
    private ResponseMessage() {}

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getResponseBody() {
        return this.responseBody;
    }
}
