package worth.server;

import java.nio.ByteBuffer;

/**
 * Created by alessiomatricardi on 06/01/21
 *
 * Attachment di una socket nel selector
 */
public class Attachment {
    private String username;    // username dell'utente online
    private ByteBuffer buffer;  // buffer associato alla socket

    public Attachment() {
        username = null;
        buffer = null;
    }

    public String getUsername() {
        return this.username;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}
