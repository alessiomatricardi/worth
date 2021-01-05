package worth.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import worth.data.Project;
import worth.data.User;
import worth.data.UserStatus;
import worth.exceptions.AlreadyLoggedException;
import worth.exceptions.UserNotExistsException;
import worth.exceptions.UsernameNotAvailableException;
import worth.exceptions.WrongPasswordException;
import worth.utils.PasswordManager;
import worth.utils.PasswordManagerImpl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Classe contenente tutti i dati dell'applicazione Worth
 * Funge da database per tutte le richieste di input/output
 */
public class PersistentData implements Registration, TCPOperations {
    private final static String STORAGE_FOLDER_PATH = "./storage/";
    private final static String PROJECTS_FOLDER_PATH = STORAGE_FOLDER_PATH + "projects/";
    private final static String USERS_FOLDER_PATH = STORAGE_FOLDER_PATH + "users/";
    private static final int BUFFER_SIZE = 1024*1024; // spazio di allocazione del buffer

    private Map<String, User> users;
    private Map<String, Project> projects;
    private Map<String, UserStatus> userStatus;

    public PersistentData() throws IOException {
        this.users = new ConcurrentHashMap<>();
        this.projects = new ConcurrentHashMap<>();
        this.userStatus = new ConcurrentHashMap<>();
        this.init();
    }

    private void init() throws IOException {
        System.out.println("Server data initialization - start");

        // gestione delle cartelle
        File directory = new File(STORAGE_FOLDER_PATH);
        if (!directory.exists()){
            directory.mkdirs();
            System.out.format("Folder %s created\n", STORAGE_FOLDER_PATH);
        }
        directory = new File(PROJECTS_FOLDER_PATH);
        if (!directory.exists()){
            directory.mkdirs();
            System.out.format("Folder %s created\n", PROJECTS_FOLDER_PATH);
        }
        directory = new File(USERS_FOLDER_PATH);
        if (!directory.exists()){
            directory.mkdirs();
            System.out.format("Folder %s created\n", USERS_FOLDER_PATH);
        }

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ObjectMapper mapper = new ObjectMapper();
        FileChannel inChannel;

        // caricamento da locale degli utenti
        int numOfUsers = 0;
        directory = new File(USERS_FOLDER_PATH);
        File[] usersList = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getPath().endsWith(".json");
            }
        });
        if (usersList != null) {
            for (File userFile : usersList) {
                buffer.clear();
                inChannel= FileChannel.open(Paths.get(userFile.getAbsolutePath()), StandardOpenOption.READ);
                boolean stop = false;
                while (!stop) {
                    if (inChannel.read(buffer) == -1){
                        stop = true;
                    }
                }
                buffer.flip();
                // lettura dal buffer dell'utente
                User user = mapper.reader().forType(new TypeReference<User>() {})
                        .readValue(buffer.array());
                this.users.put(user.getUsername(), user);
                this.userStatus.put(user.getUsername(), UserStatus.OFFLINE);
                ++numOfUsers;
            }
        }

        // caricamento da locale dei progetti todo
        int numOfProjects = 0;
        /*directory = new File(PROJECTS_FOLDER_PATH);
        File[] projectsList = directory.listFiles(File::isDirectory);
        if (projectsList != null) {
            for (File project : projectsList) {
                File projectFile =
                inChannel= FileChannel.open(Paths.get(user.getAbsolutePath()), StandardOpenOption.READ);
                boolean stop = false;
                while (!stop) {
                    if (inChannel.read(buffer) == -1){
                        stop = true;
                    }
                }
            }
            this.users = mapper.reader()
                    .forType(new TypeReference<Map<String, User>>() {})
                    .readValue(buffer.array());	 // lettura dal buffer
            numOfProjects++;
        }*/

        System.out.format("%d users retrieved\n", numOfUsers);
        System.out.format("%d projects retrieved\n", numOfProjects);
        System.out.println("Server data initialization - successful");
    }

    @Override
    public void registerUser(String username, String hash, String salt) throws UsernameNotAvailableException {
        User newUser = new User(username, hash, salt);
        if (this.users.putIfAbsent(username, newUser) != null)
            throw new UsernameNotAvailableException();
        this.userStatus.put(username, UserStatus.OFFLINE);
        String fileName = USERS_FOLDER_PATH + username + ".json";
        // salvataggio dell'utente su file
        ObjectMapper mapper = new ObjectMapper();
        try (FileChannel outChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            byte[] byteUser = mapper.writeValueAsBytes(newUser);

            ByteBuffer bb = ByteBuffer.wrap(byteUser);
            while (bb.hasRemaining())
                outChannel.write(bb);
        } catch (IOException e) {
            e.printStackTrace();
            this.users.remove(username);
        }
    }

    @Override
    public void login(String username, String password)
            throws UserNotExistsException, AlreadyLoggedException, WrongPasswordException {
        User theUser = this.users.get(username);
        if (theUser == null) {
            throw new UserNotExistsException();
        } else {
            UserStatus status = this.userStatus.get(username);
            if (status == UserStatus.ONLINE) {
                throw new AlreadyLoggedException();
            }
            String hash = theUser.getHash();
            String salt = theUser.getSalt();
            PasswordManager passwordManager = new PasswordManagerImpl();
            if (!passwordManager.isExpectedPassword(password, salt, hash))
                throw new WrongPasswordException();
            this.userStatus.replace(username, UserStatus.ONLINE);
        }
    }

    @Override
    public Map<String, UserStatus> getUserStatus() {
        return this.userStatus;
    }
}
