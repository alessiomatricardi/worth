package worth.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import worth.data.Card;
import worth.data.Project;
import worth.data.User;
import worth.data.UserStatus;
import worth.exceptions.*;
import worth.utils.MulticastAddressManager;
import worth.utils.PasswordManager;
import worth.utils.PasswordManagerImpl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
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
public class PersistentData implements Registration, TCPOperations {
    private final static String STORAGE_FOLDER_PATH = "./storage/";
    private final static String PROJECTS_FOLDER_PATH = STORAGE_FOLDER_PATH + "projects/";
    private final static String USERS_FOLDER_PATH = STORAGE_FOLDER_PATH + "users/";
    private final static String PROJECT_FILENAME = "info.json";
    private static final int BUFFER_SIZE = 1024*1024; // spazio di allocazione del buffer

    private Map<String, User> users;
    private Map<String, Project> projects;
    private Map<String, UserStatus> userStatus;
    private ObjectMapper mapper;

    public PersistentData() throws IOException {
        this.users = new ConcurrentHashMap<>();
        this.projects = new ConcurrentHashMap<>();
        this.userStatus = new ConcurrentHashMap<>();

        // Jackson object
        mapper = new ObjectMapper();
        // abilita indentazione
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // formattazione data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        mapper.setDateFormat(dateFormat);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

        // alloco buffer
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        // FileFilter per filtraggio file da dover elaborare
        // solo file con estensione .json
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getPath().endsWith(".json");
            }
        };

        // caricamento da locale degli utenti
        int numOfUsers = 0;
        directory = new File(USERS_FOLDER_PATH);
        // get lista utenti
        File[] usersList = directory.listFiles(fileFilter);
        if (usersList != null) {
            for (File userFile : usersList) {
                // elaboro file utente
                this.readFile(userFile, buffer);

                // lettura dal buffer dell'utente
                User user = mapper.reader().forType(new TypeReference<User>() {})
                        .readValue(buffer.array());

                // inserisco nelle strutture dati
                this.users.put(user.getUsername(), user);
                this.userStatus.put(user.getUsername(), UserStatus.OFFLINE);
                ++numOfUsers;
            }
        }

        int numOfProjects = 0;
        directory = new File(PROJECTS_FOLDER_PATH);
        // get lista delle directories di progetto
        File[] projectsList = directory.listFiles(File::isDirectory);
        if (projectsList != null) {
            for (File projectDir : projectsList) {
                // get files del singolo progetto
                // ogni progetto contiene il file di progetto
                // e i files delle cards
                File[] projectFileList = projectDir.listFiles(fileFilter);
                if (projectFileList != null) {
                    if (projectFileList.length == 0) {
                        // todo vuoto impossibile
                    }
                    // lista delle card del progetto
                    List<Card> projectCardList = new ArrayList<>();
                    File projectInfo = null;
                    // elaboro cards
                    for (File cardFile : projectFileList) {
                        // se il file è quello del progetto, lo salvo e lo elaboro alla fine
                        if (cardFile.getName().equals(PROJECT_FILENAME)) {
                            projectInfo = cardFile;
                            continue;
                        }

                        // elaboro file card
                        this.readFile(cardFile, buffer);

                        // lettura dal buffer della card
                        Card card = mapper.reader().forType(new TypeReference<Card>() {})
                                .readValue(buffer.array());
                        projectCardList.add(card);
                    }

                    // ora posso elaborare il progetto

                    this.readFile(projectInfo, buffer);

                    // lettura dal buffer del progetto
                    Project project = mapper.reader().forType(new TypeReference<Project>() {})
                            .readValue(buffer.array());
                    try {
                        project.initCardList(projectCardList);
                        project.initChatAddress(MulticastAddressManager.getAddress());
                    } catch (NoSuchAddressException e) {
                        e.printStackTrace();
                    }
                    this.projects.put(project.getName(), project);
                    ++numOfProjects;
                }
            }
        }

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

        // salvataggio dell'utente su file
        try {
            this.storeUser(newUser);
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
    public void logout(String username) throws UserNotExistsException {
        User theUser = this.users.get(username);
        if (theUser == null) {
            throw new UserNotExistsException();
        } else {
            this.userStatus.replace(username, UserStatus.OFFLINE);
        }
    }

    @Override
    public Map<String, UserStatus> getUserStatus() {
        return this.userStatus;
    }

    /**
     * Salva l'utente user nello storage serializzandolo
     *
     * @param user utente da salvare
     *
     * @throws IOException se ci sono errori nel salvataggio
     */
    private void storeUser(User user) throws IOException {
        String fileName = USERS_FOLDER_PATH + user.getUsername() + ".json";
        FileChannel outChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byte[] byteUser = mapper.writeValueAsBytes(user);

        ByteBuffer bb = ByteBuffer.wrap(byteUser);
        while (bb.hasRemaining())
            outChannel.write(bb);
    }

    /**
     * Salva la card nello storage del progetto projectName
     *
     * @param card card da salvare
     * @param projectName nome del progetto a cui la card fa riferimento
     *
     * @throws IOException se ci sono errori nel salvataggio
     */
    private void storeCard(Card card, String projectName) throws IOException {
        String fileName = PROJECTS_FOLDER_PATH + projectName + "/card_" + card.getName() + ".json";
        FileChannel outChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byte[] byteCard = mapper.writeValueAsBytes(card);

        ByteBuffer bb = ByteBuffer.wrap(byteCard);
        while (bb.hasRemaining())
            outChannel.write(bb);
    }

    /**
     * Salva il progetto project nello storage
     *
     * @param project progetto da salvare
     *
     * @throws IOException se ci sono errori nel salvataggio
     */
    private void storeProject(Project project) throws IOException {
        String fileName = PROJECTS_FOLDER_PATH + project.getName() + "/" + PROJECT_FILENAME;
        FileChannel outChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byte[] byteProject = mapper.writeValueAsBytes(project);

        ByteBuffer bb = ByteBuffer.wrap(byteProject);
        while (bb.hasRemaining())
            outChannel.write(bb);
    }

    private void readFile(File file, ByteBuffer buffer) throws IOException {
        FileChannel inChannel;
        if (file == null)
            throw new IOException(); // impossibile non esista
        buffer.clear();
        inChannel= FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.READ);
        boolean stop = false;
        while (!stop) {
            if (inChannel.read(buffer) == -1){
                stop = true;
            }
        }
        buffer.flip();
    }
}
