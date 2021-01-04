package worth;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * Created by alessiomatricardi on 04/01/21
 */
// todo interface
public class ResponseMessage implements Serializable {
    private int statusCode;
    private String responseBody;

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
