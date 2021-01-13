package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.HostsCardsContainer;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;
import worth.client.ui.loggedPanels.*;
import worth.client.ui.loggedPanels.projectPanels.*;
import worth.client.ui.loggedPanels.ProjectDetailsPanel;
import worth.data.*;
import worth.exceptions.*;
import worth.protocol.CommunicationProtocol;
import worth.utils.UIMessages;
import worth.utils.Utils;

import javax.swing.*;
import java.net.UnknownHostException;
import java.util.*;
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
        projectDetailsPanel.getCardsButton().addActionListener(e -> this.showCards());
        projectDetailsPanel.getMembersButton().addActionListener(e -> this.showMembers());
        projectDetailsPanel.getChatButton().addActionListener(e -> this.showChat());
        projectDetailsPanel.getAddCardButton().addActionListener(e -> this.showAddCard());

        // azioni possibili in panel dentro ProjectDetailsPanel

        // dentro MemberPanel posso aggiungere un membro al progetto
        MembersPanel membersPanel = projectDetailsPanel.getMembersPanel();
        membersPanel.getAddMemberButton().addActionListener(e -> this.addMember());

        // dentro CardsPanel posso vedere la lista delle cards
        CardsPanel cardsPanel = projectDetailsPanel.getCardsPanel();

        // dentro AddCardPanel posso aggiungere una card al progetto
        AddCardPanel addCardPanel = projectDetailsPanel.getAddCardPanel();
        addCardPanel.getAddCardButton().addActionListener(e -> this.addCard());

        // dentro CardDetailsPanel posso vedere i dettagli di una card e spostarla
        CardDetailsPanel cardDetailsPanel = projectDetailsPanel.getCardDetailsPanel();
        cardDetailsPanel.getMoveCardButton().addActionListener(e -> this.moveCard());

        // dentro ChatPanel posso vedere la chat del progetto
        // send message todo

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
        this.showMembers();

        this.showCard(this.view, LoggedUI.PROJECT_DETAILS_PANEL);
    }

    // visualizza cards dentro ProjectDetails

    private void showCards() {
        try {
            Map<CardStatus, List<String>> cards = this.model.showCards(this.selectedProject);
            Map<CardStatus, List<JButton>> cardButtons = new HashMap<>();

            // genero buttons con azioni
            CardStatus[] values = CardStatus.values();
            for (CardStatus status : values) {
                List<String> statusList = cards.get(status);
                List<JButton> buttonList = new ArrayList<>();
                cardButtons.put(status, buttonList);
                if (statusList != null) {
                    for (String cardName : statusList) {
                        // creo button
                        JButton button = new JButton(cardName);
                        // aggiungo azione
                        button.addActionListener(e -> this.showCardDetails(cardName));
                        // lo aggiungo alla lista
                        buttonList.add(button);
                    }
                }
            }

            ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();
            CardsPanel cardsPanel = detailsPanel.getCardsPanel();

            cardsPanel.setUI(cardButtons);
            this.updateUI(cardsPanel);

            this.showCard(detailsPanel, ProjectDetailsPanel.CARDS_PANEL);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        }
    }

    private void showMembers() {
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

    private void showCardDetails(String cardName) {
        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();
        CardDetailsPanel cardDetailsPanel = detailsPanel.getCardDetailsPanel();
        try {
            CardNoMovs card = this.model.showCard(this.selectedProject, cardName);

            // modifico variabili interne (di utilità quando viene chiamato moveCard)
            cardDetailsPanel.setCardName(cardName);
            cardDetailsPanel.setFromStatus(card.getStatus());

            // cerco di ottenere anche i movimenti della card
            List<Movement> movements = this.model.getCardHistory(this.selectedProject, cardName);

            // costruisco UI
            //cardDetailsPanel.setUI(); todo
            this.updateUI(cardDetailsPanel);

            this.showCard(detailsPanel, ProjectDetailsPanel.CARD_DETAILS_PANEL);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (CardNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.CARD_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        }
    }

    private void showAddCard() {
        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();

        this.showCard(detailsPanel, ProjectDetailsPanel.ADD_CARD_PANEL);
    }

    private void showChat() {
        try {
            String chatAddress = this.model.readChat(this.selectedProject);

            /*
             * se chatAddress non nullo, istanzio un thread che si mette in ascolto dei messaggi
             * in arrivo su quella chat
             * inoltre, devo creare la card associata a quella specifica chat
             */
            if (chatAddress != null) {
                // creo card
                ChatLog chatLog = new ChatLog();

                // thread da mandare in esecuzione
                ChatReaderControllerTask chatReaderControllerTask = new ChatReaderControllerTask(
                        this.model.getUsername(),
                        this.model.getMulticastSocket(),
                        chatAddress,
                        CommunicationProtocol.UDP_CHAT_PORT,
                        chatLog
                );
                ExecutorService threadPool = this.model.getThreadPool();
                threadPool.execute(chatReaderControllerTask);

                // aggiungo card il cui nome è il nome del progetto
                ChatPanel chatPanel = this.view.getProjectDetailsPanel().getChatPanel();
                chatPanel.addChatLog(this.selectedProject, chatLog);
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

    // altre operazioni

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

            // torno alla home
            this.showHome();
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

            // refresh UI membri progetto
            this.showMembers();
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        } catch (UserAlreadyPresentException e) {
            Utils.showErrorMessageDialog(UIMessages.USER_ALREADY_PRESENT);

            // refresh UI membri progetto
            this.showMembers();
        } catch (UserNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.USERNAME_NOT_EXISTS);
        }
    }

    private void addCard() {
        ProjectDetailsPanel detailsPanel = this.view.getProjectDetailsPanel();
        AddCardPanel addCardPanel = detailsPanel.getAddCardPanel();
        String cardName = addCardPanel.getCardName().getText();
        String description = addCardPanel.getCardDescription().getText();

        try {
            this.model.addCard(this.selectedProject, cardName, description);

            addCardPanel.getCardName().setText("");
            addCardPanel.getCardDescription().setText("");

            // visualizzo lista cards aggiornata
            this.showCards();
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        } catch (CardAlreadyExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.CARD_ALREADY_EXISTS);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (CharactersNotAllowedException e) {
            Utils.showErrorMessageDialog(UIMessages.CHARACTERS_NOT_ALLOWED);
        }
    }

    private void moveCard() {
        ProjectDetailsPanel projectDetailsPanel = this.view.getProjectDetailsPanel();
        CardDetailsPanel cardDetailsPanel = projectDetailsPanel.getCardDetailsPanel();
        String cardName = cardDetailsPanel.getCardName();
        CardStatus from = cardDetailsPanel.getFromStatus();
        CardStatus to = (CardStatus) cardDetailsPanel.getToStatusComboBox().getSelectedItem();

        // se l'utente non seleziona alcuno stato di arrivo
        if (to == null) {
            Utils.showErrorMessageDialog(UIMessages.OPERATION_NOT_ALLOWED);
            return;
        }

        try {
            this.model.moveCard(this.selectedProject, cardName, from, to);

            // visualizzo schermata movimenti aggiornata
            this.showCardDetails(cardName);
        } catch (CommunicationException e) {
            Utils.showErrorMessageDialog(UIMessages.CONNECTION_ERROR);
        } catch (ProjectNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.PROJECT_NOT_EXISTS);
        } catch (UnauthorizedUserException e) {
            Utils.showErrorMessageDialog(UIMessages.UNAUTHORIZED_USER);
        } catch (OperationNotAllowedException e) {
            Utils.showErrorMessageDialog(UIMessages.OPERATION_NOT_ALLOWED);

            // visualizzo schermata movimenti aggiornata
            this.showCardDetails(cardName);
        } catch (CardNotExistsException e) {
            Utils.showErrorMessageDialog(UIMessages.CARD_NOT_EXISTS);
        }
    }

    private void logout() {
        try {
            this.model.logout();

            // torno a pannello di login
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
