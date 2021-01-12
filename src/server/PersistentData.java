package worth.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import worth.data.*;
import worth.exceptions.*;
import worth.utils.MulticastAddressManager;
import worth.utils.MyObjectMapper;
import worth.utils.PasswordManager;
import worth.utils.PasswordManagerImpl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    private final static String PROJECT_CONFIG_FILENAME = "info.json";
    private static final int BUFFER_SIZE = 1024*1024; // spazio di allocazione del buffer

    private final Map<String, User> users;
    private final Map<String, Project> projects;
    private final Map<String, UserStatus> userStatus;
    private final ObjectMapper mapper;

    public PersistentData() throws IOException {
        this.users = new ConcurrentHashMap<>();
        this.projects = new ConcurrentHashMap<>();
        this.userStatus = new ConcurrentHashMap<>();

        // Jackson object
        mapper = new MyObjectMapper();

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
        FileFilter fileFilter =
                pathname -> pathname.isFile() && pathname.getPath().endsWith(".json");

        // caricamento da locale degli utenti
        int numOfUsers = 0;
        directory = new File(USERS_FOLDER_PATH);
        // get lista utenti
        File[] usersList = directory.listFiles(fileFilter);
        if (usersList != null) {
            for (File userFile : usersList) {
                // carico utente nella struttura dati
                this.loadUser(userFile, buffer);

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
                        System.out.format("Project %s doesn't have config file (%s)\n",
                                projectDir.getName(), PROJECT_CONFIG_FILENAME);
                    }
                    // lista delle card del progetto
                    List<Card> projectCardList = new ArrayList<>();
                    File projectInfo = null;
                    // elaboro cards
                    for (File cardFile : projectFileList) {
                        // se il file è quello del progetto, lo salvo e lo elaboro alla fine
                        if (cardFile.getName().equals(PROJECT_CONFIG_FILENAME)) {
                            projectInfo = cardFile;
                            continue;
                        }

                        // carico la card nella lista delle card del progetto
                        this.loadCard(cardFile, buffer, projectCardList);
                    }

                    // ora posso elaborare il progetto
                    // lo carico sulla struttura dati
                    try {
                        this.loadProject(projectInfo, buffer, projectCardList);
                    } catch (NoSuchAddressException e) {
                        System.out.println("There are no more multicast addresses...");
                        throw new IOException();
                    } catch (AlreadyInitialedException e) {
                        System.out.println("Project seems to be already initialized...");
                        throw new IOException();
                    }

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
    public List<Project> listProjects(String username) throws UserNotExistsException {
        if (!this.users.containsKey(username))
            throw new UserNotExistsException();
        List<Project> toReturn = new ArrayList<>();
        for (String key : this.projects.keySet()) {
            Project p = this.projects.get(key);
            if (p.getMembers().contains(username))
                toReturn.add(p);
        }
        return toReturn;
    }

    @Override
    public void createProject(String projectName, String whoRequest)
            throws ProjectAlreadyExistsException, NoSuchAddressException, IOException {
        if (this.projects.containsKey(projectName))
            throw new ProjectAlreadyExistsException();
        Project newProject = new Project(projectName, whoRequest);
        this.storeProject(newProject);
        this.projects.put(projectName, newProject);
    }

    @Override
    public void addMember(String projectName, String username, String whoRequest)
            throws ProjectNotExistsException, UnauthorizedUserException, UserAlreadyPresentException, UserNotExistsException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        if (!this.users.containsKey(username))
            throw new UserNotExistsException();
        project.addMember(username);
    }

    @Override
    public List<String> showMembers(String projectName, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        List<String> members = project.getMembers();
        if (!members.contains(whoRequest))
            throw new UnauthorizedUserException();
        return members;
    }

    @Override
    public List<String> showCards(String projectName, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        List<Card> cards = project.getAllCards();
        List<String> toReturn = new ArrayList<>();
        for (Card card : cards) {
            toReturn.add(card.getName());
        }
        return toReturn;
    }

    @Override
    public Card showCard(String projectName, String cardName, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException, CardNotExistsException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        return project.getCard(cardName);
    }

    @Override
    public void addCard(String projectName, String cardName, String description, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException, CardAlreadyExistsException, IOException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        Card newCard = new Card(cardName, description);
        project.addCard(newCard);

        this.storeCard(newCard, projectName);
        this.storeProject(project);
    }

    @Override
    public void moveCard(String projectName, String cardName, CardStatus from, CardStatus to, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException, CardNotExistsException, OperationNotAllowedException, IOException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        project.moveCard(cardName, from, to);
        Card moved = project.getCard(cardName);

        this.storeCard(moved, projectName);
        this.storeProject(project);
    }

    @Override
    public List<Movement> getCardHistory(String projectName, String cardName, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException, CardNotExistsException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        Card card = project.getCard(cardName);
        return card.getMovements();
    }

    @Override
    public String readChat(String projectName, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        return project.getChatAddress();
    }

    @Override
    public void cancelProject(String projectName, String whoRequest) throws ProjectNotExistsException, UnauthorizedUserException, ProjectNotCancelableException {
        Project project;
        if ((project = this.projects.get(projectName)) == null)
            throw new ProjectNotExistsException();
        if (!project.getMembers().contains(whoRequest))
            throw new UnauthorizedUserException();
        if (!project.isCancelable())
            throw new ProjectNotCancelableException();
        // libero indirizzo multicast
        MulticastAddressManager.freeAddress(project.getChatAddress());
        // rimuovo progetto
        this.projects.remove(projectName);

        // rimozione files del progetto
        File projectDir = new File(PROJECTS_FOLDER_PATH + projectName);
        if (projectDir.exists() && projectDir.isDirectory()) {
            // elimino tutti i file al suo interno
            File[] files = projectDir.listFiles();
            for (File file : files) {
                file.delete();
            }
            // elimino directory
            projectDir.delete();
        }
    }

    @Override
    public Map<String, UserStatus> getUserStatus() {
        return this.userStatus;
    }

    @Override
    public String getProjectChatAddress(String projectName) throws ProjectNotExistsException {
        Project project;
        project = this.projects.get(projectName);
        if (project == null)
            throw new ProjectNotExistsException();
        return project.getChatAddress();
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
        String projectFolder = PROJECTS_FOLDER_PATH + project.getName() + "/";
        String fileName = projectFolder + PROJECT_CONFIG_FILENAME;
        File projectFolderFile = new File(projectFolder);
        if (!projectFolderFile.exists()) {
            projectFolderFile.mkdir();
        }
        FileChannel outChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byte[] byteProject = mapper.writeValueAsBytes(project);

        ByteBuffer bb = ByteBuffer.wrap(byteProject);
        while (bb.hasRemaining())
            outChannel.write(bb);
    }

    /**
     * Deserializzazione dell'utente definito dal file userFile
     *
     * @param userFile file dell'utente
     * @param buffer buffer utilizzato per caricamento
     *
     * @throws IOException se ci sono errori nel caricamento
     */
    private void loadUser(File userFile, ByteBuffer buffer) throws IOException {
        // carico il file sul buffer
        this.readFile(userFile, buffer);

        // lettura dal buffer dell'utente
        User user = mapper.reader().forType(new TypeReference<User>() {})
                .readValue(buffer.array());

        // inserisco nelle strutture dati
        this.users.put(user.getUsername(), user);
        this.userStatus.put(user.getUsername(), UserStatus.OFFLINE);
    }

    /**
     * Deserializzazione di una card definita dal file cardFile
     *
     * @param cardFile file della card
     * @param buffer buffer utilizzato per caricamento
     * @param projectCardList lista delle card del progetto che stiamo elaborando
     *
     * @throws IOException se ci sono errori nel caricamento
     */
    private void loadCard(File cardFile, ByteBuffer buffer, List<Card> projectCardList)
            throws IOException {
        // carico il file sul buffer
        this.readFile(cardFile, buffer);

        // lettura dal buffer della card
        Card card = mapper.reader().forType(new TypeReference<Card>() {})
                .readValue(buffer.array());
        projectCardList.add(card);

    }

    /**
     * Deserializzazione di un progetto definito dal file projectInfo
     *
     * @param projectInfo file del progetto
     * @param buffer buffer utilizzato per caricamento
     * @param projectCardList lista delle card del progetto che stiamo elaborando
     *
     * @throws IOException se ci sono errori nel caricamento
     * @throws AlreadyInitialedException
     * se alcuni campi del progetto non possono essere inizializzati perchè lo sono già
     */
    private void loadProject(File projectInfo, ByteBuffer buffer, List<Card> projectCardList)
            throws IOException, NoSuchAddressException, AlreadyInitialedException {
        // carico il file sul buffer
        this.readFile(projectInfo, buffer);

        // lettura dal buffer del progetto
        Project project = mapper.reader().forType(new TypeReference<Project>() {})
                .readValue(buffer.array());

        project.initCardList(projectCardList);
        project.initChatAddress(MulticastAddressManager.getAddress());

        this.projects.put(project.getName(), project);
    }

    /**
     * Caricamento del file sul buffer
     *
     * @param file file da dover caricare
     * @param buffer buffer interessato del caricamento
     *
     * @throws IOException se ci sono errori nel caricamento
     *
     */
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
