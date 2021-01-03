import exceptions.UsernameNotAvailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Classe contenente tutti i dati dell'applicazione Worth
 * Funge da database per tutte le richieste di input/output
 */
public class PersistentData implements Registration {
    private final static String PROJECTS_FOLDER_PATH = "";
    private final static String USERS_FOLDER_PATH = "";

    private final Map<String, User> users;
    private final Map<String, Project> projects;
    private final List<String> onlineUsers;

    private PersistentData() {
        users = new ConcurrentHashMap<>();
        projects = new ConcurrentHashMap<>();
        onlineUsers = new ArrayList<>();
    }

    public static PersistentData init() {
        PersistentData persistentData = new PersistentData();
        // caricamento da locale degli utenti
        // caricamento da locale dei progetti
        return persistentData;
    }

    @Override
    public void registerUser(String username, String hash) throws UsernameNotAvailableException {
        User newUser = new User(username, hash);
        if (users.putIfAbsent(username, newUser) != null)
            throw new UsernameNotAvailableException();
        // todo crea file
    }
}
