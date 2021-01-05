package worth.data;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Utente di Worth
 */
public class User implements Serializable {
    private String username;
    private String hash;
    private String salt;

    public User(String username, String hashPassword, String salt) {
        this.username = username;
        this.hash = hashPassword;
        this.salt = salt;
    }

    @JsonCreator
    private User() {}

    public String getUsername() {
        return this.username;
    }

    public String getHash() {
        return this.hash;
    }

    public String getSalt() {
        return this.salt;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.username.equals(((User)o).getUsername());
    }
}
