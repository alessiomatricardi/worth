/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Utente di Worth
 */
public class User {
    private String username;
    private String hash; // formato hashPassword:salt

    public User(String username, String hashPassword) {
        this.username = username;
        this.hash = hashPassword;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHash() {
        return this.hash;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.username.equals(((User)o).getUsername());
    }
}
