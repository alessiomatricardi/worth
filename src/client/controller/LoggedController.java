package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.HostsCardsContainer;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.client.ui.loggedPanels.*;
import worth.client.ui.loggedPanels.projectPanels.ChatCard;
import worth.client.ui.loggedPanels.ProjectDetailsPanel;
import worth.client.ui.loggedPanels.projectPanels.ChatPanel;
import worth.client.ui.loggedPanels.projectPanels.MembersPanel;
import worth.data.Project;
import worth.data.UserStatus;
import worth.exceptions.*;
import worth.protocol.CommunicationProtocol;
import worth.utils.UIMessages;
import worth.utils.Utils;

import javax.swing.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Controller che gestisce la UI dell'utente online
 */
public class LoggedController {
    private final ClientModel model;
    private final LoggedUI view;
    private String selectedProject; // ultimo progetto visualizzato dall'utente

    public LoggedController(ClientModel model, LoggedUI view) {
        this.model = model;
        this.view = view;
        this.initController();
    }

    private void initController() {
        // azioni possibili in LoggedUI

        this.view.getHomeButton().addActionListener(e -> this.showHome());

        this.view.getUserListButton().addActionListener(e -> this.showUsers(false));

        this.view.getProjectsListButton().addActionListener(e -> this.showProjectsList());

        this.view.getLogoutButton().addActionListener(e -> {
            int result = Utils.showQuestionMessageDialog(UIMessages.LOGOUT_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                this.logout();
            }
        });

        // azioni possibili in panel contenuti dentro la view

        // dentro HomePanel posso creare un progetto
        HomePanel homePanel = this.view.getHomePanel();
        homePanel.getCreateProjectButton().addActionListener(e -> this.createProject());

        // dentro UsersPanel posso vedere la lista degli utenti
        UsersPanel usersPanel = this.view.getUsersPanel();
        usersPanel.getOnlineToggle().addActionListener(e -> {
            if (usersPanel.getOnlineToggle().getText().equals(UsersPanel.SHOW_ALL_USERS_TEXT)) {
                // voglio vedere tutti gli utenti
                this.showUsers(false);
                usersPanel.getOnlineToggle().setText(UsersPanel.SHOW_ONLINE_USERS_TEXT);
            } else {
                // voglio vedere solo gli utenti online
                this.showUsers(true);
                usersPanel.getOnlineToggle().setText(UsersPanel.SHOW_ALL_USERS_TEXT);
            }
        });

        // dentro ProjectsListPanel posso vedere la lista dei progetti dell'utente online
        // l'inizializzazione dei suoi buttons viene fatta a runtime
        // poichè la lista dei progetti può variare ogni volta
        ProjectsListPanel projectsListPanel = this.view.getProjectsListPanel();

        // dentro ProjectsDetailsPanel posso vedere i dettagli di un singolo progetto
        ProjectDetailsPanel projectDetailsPanel = this.view.getProjectDetailsPanel();
        projectDetailsPanel.getCancelButton().addActionListener(e -> this.cancelProject());
        projectDetailsPanel.getCardsButton().addActionListener(e -> this.showProjectCards());
        projectDetailsPanel.getMembersButton().addActionListener(e -> this.showProjectMembers());
        projectDetailsPanel.getChatButton().addActionListener(e -> this.showProjectChat());
        projectDetailsPanel.getAddCardButton().addActionListener(e -> this.showAddCardPanel());

        // azioni possibili in panel dentro ProjectDetailsPanel

        MembersPanel membersPanel = projectDetailsPanel.getMembersPanel();
        membersPanel.getAddMemberButton().addActionListener(e -> this.addMember());

    }

    // visualizza cards dentro LoggedUI

    private void showHome() {
        this.showCard(this.view, LoggedUI.HOME_PANEL);
    }

    private void showUsers(boolean onlyOnlineUsers) {
        List<JLabel> labels = new ArrayList<>();
        if (onlyOnlineUsers) {
            List<String> users = this.model.listOnlineUsers();
            for (String user : users) {
                String text = user + " : ONLINE";
                labels.add(new JLabel(text));
            }
        } else {
            Map<String, UserStatus> userStatus= this.model.listUsers();
            Set<String> users = userStatus.keySet();
            for (String user : users) {
                String text = user + " : " + userStatus.get(user).name();
                labels.add(new JLabel(text));
            }
        }
        UsersPanel usersPanel = this.view.getUsersPanel();
        usersPanel.setUI(labels);
        // aggiorno UI
        this.updateUI(usersPanel);
        this.showCard(this.view, LoggedUI.USERS_PANEL);
    }

    private void showProjectsList() {
        try {
            List<Project> projects = this.model.listProjects();
            // creo buttons
            List<JButton> buttons = new ArrayList<>();
            for (Project project : projects) {
                // creo bottone con nome del progetto
                JButton button = new JButton(project.getName());
                // aggiungo azione
                // quando clicco sul bottone, vado ai dettagli del progetto
                button.addActionListener(e -> this.showProjectDetails(button.getText()));
                // aggiungo alla lista
                buttons.add(button);
            }
            // aggiungo elementi
            ProjectsListPanel projectsListPanel = this.view.getProjectsListPanel();
            projectsListPanel.setUI(buttons);
            // aggiorno UI
            this.updateUI(projectsListPanel);
            this.showCard(this.view, LoggedUI.PROJECTS_PANEL);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        }
    }

    private void showProjectDetails(String projectName) {
        // voglio visualizzare il progetto projectName
        this.selectedProject = projectName;

        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();

        // aggiorno UI
        detailsPanel.getProjectNameLabel().setText(this.selectedProject);
        this.updateUI(detailsPanel);

        // mostro card dei membri
        this.showProjectMembers();

        this.showCard(this.view, LoggedUI.PROJECT_DETAILS_PANEL);
    }

    // visualizza cards dentro ProjectDetails

    private void showProjectCards() {
        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();

        this.showCard(detailsPanel, ProjectDetailsPanel.CARDS_PANEL);
    }

    private void showProjectMembers() {
        try {
            List<String> members = this.model.showMembers(this.selectedProject);
            List<JLabel> labels = new ArrayList<>();

            for (String member : members) {
                labels.add(new JLabel(member));
            }

            ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();
            MembersPanel membersPanel = detailsPanel.getMembersPanel();

            // modifica e aggiorna UI
            membersPanel.setUI(labels);
            this.updateUI(membersPanel);

            // mostra
            this.showCard(detailsPanel, ProjectDetailsPanel.MEMBERS_PANEL);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        }
    }

    private void showCardDetails() {
        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();

        this.showCard(detailsPanel, ProjectDetailsPanel.CARD_DETAILS_PANEL);
    }

    private void showAddCardPanel() {

    }

    private void showProjectChat() {
        try {
            String chatAddress = this.model.readChat(this.selectedProject);
            /**
             * se chatAddress non nullo, istanzio un thread che si mette in ascolto dei messaggi
             * in arrivo su quella chat
             * inoltre, devo creare la card associata a quella specifica chat
             */
            if (chatAddress != null) {
                // creo card
                ChatCard chatCard = new ChatCard();

                // thread da mandare in esecuzione
                ChatReaderControllerTask chatReaderControllerTask = new ChatReaderControllerTask(
                        this.model.getUsername(),
                        this.model.getMulticastSocket(),
                        chatAddress,
                        CommunicationProtocol.UDP_CHAT_PORT,
                        chatCard
                );
                ExecutorService threadPool = this.model.getThreadPool();
                threadPool.execute(chatReaderControllerTask);

                // aggiungo card il cui nome è il nome del progetto
                ChatPanel chatPanel = this.view.getProjectDetailsPanel().getChatPanel();
                chatPanel.addChatCard(this.selectedProject, chatCard);
            }

            // visualizza quella card

            ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();
            ChatPanel chatPanel = detailsPanel.getChatPanel();

            // visualizzo chat del progetto selectedProject
            this.showCard(chatPanel, this.selectedProject);

            // visualizzo pannello chat
            this.showCard(detailsPanel, ProjectDetailsPanel.CHAT_PANEL);
        } catch (UnknownHostException | CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        }
    }

    private void createProject() {
        HomePanel homePanel = this.view.getHomePanel();
        String projectName = homePanel.getProjectNameField().getText();
        if (projectName.isBlank()) {
            Utils.showErrorMessageDialog(UIMessages.EMPTY_FIELD);
            return;
        }
        try {
            this.model.createProject(projectName);
            Utils.showInfoMessageDialog(UIMessages.PROJECT_CREATE_SUCCESS);
            // resetto campo
            homePanel.getProjectNameField().setText("");
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (NoSuchAddressException e) {
            Utils.showErrorMessageDialog(UIMessages.NO_SUCH_ADDRESS);
        } catch (ProjectAlreadyExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_ALREADY_EXISTS);
        } catch (CharactersNotAllowedException e) {
            Utils.showErrorMessageDialog(UIMessages.CHARACTERS_NOT_ALLOWED);
        }
    }

    private void cancelProject() {
        try {
            this.model.cancelProject(this.selectedProject);
            Utils.showInfoMessageDialog(UIMessages.PROJECT_CANCEL_SUCCESS);

            // torno alla lista dei miei progetti
            this.showProjectsList();
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        } catch (ProjectNotCancelableException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_CANCELABLE);
        }
    }

    private void addMember() {
        MembersPanel membersPanel = this.view.getProjectDetailsPanel().getMembersPanel();
        String userToAdd = membersPanel.getAddMemberField().getText();

        try {
            this.model.addMember(this.selectedProject, userToAdd);

            Utils.showInfoMessageDialog(UIMessages.ADD_MEMBER_SUCCESS);

            this.showProjectMembers();
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        } catch (UserAlreadyPresentException e) {
            Utils.showErrorMessageDialog(UIMessages.USER_ALREADY_PRESENT);
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        }
    }

    private void logout() {
        try {
            this.model.logout();
            this.changeContext();
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        }
    }

    private void updateUI(JPanel panel) {
        panel.revalidate();
        panel.repaint();
    }

    // visualizza la card cardName all'interno del panel hostPanel
    private void showCard(HostsCardsContainer hostPanel, String cardName) {
        hostPanel.getCardLayout().show(hostPanel.getCardContainer(), cardName);
    }

    // torna alla schermata di login
    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
