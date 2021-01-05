package worth;

import java.io.Serializable;

/**
 * Created by alessiomatricardi on 05/01/21
 */
public class RegisteredUser implements Serializable {
    private final String username;
    private final boolean online;

    public RegisteredUser(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }
}
