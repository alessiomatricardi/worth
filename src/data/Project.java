package worth.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import worth.exceptions.*;
import worth.protocol.CommunicationProtocol;
import worth.utils.MulticastAddressManager;
import worth.utils.PortManager;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Progetto di Worth
 */
public class Project implements Serializable {
    private String name;
    private List<String> members;
    private LocalDateTime creationDateTime;
    /**
     * l'indirizzo della chat multicast non viene mai serializzato/deserializzato
     * quando il progetto viene recuperato dal server, gli viene assegnato un nuovo indirizzo
     */
    @JsonIgnore
    private String chatAddress;
    // stesso trattamento riservato alle porte
    @JsonIgnore
    private int chatPort;
    private Map<CardStatus, List<String>> statusLists; // 4 liste
    /**
     * le card del progetto vengono serializzate in un file per ognuna
     */
    @JsonIgnore
    private List<Card> cards;

    @JsonCreator
    public Project() {}

    public Project(String projectName, String creator) throws NoSuchAddressException, NoSuchPortException {
        this.name = projectName;
        this.members = new ArrayList<>();
        this.members.add(creator);
        this.creationDateTime = LocalDateTime.now(CommunicationProtocol.ZONE_ID);
        this.chatAddress = MulticastAddressManager.getAddress();
        this.chatPort = PortManager.getPort();
        this.statusLists = new HashMap<>();
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            this.statusLists.put(status, new ArrayList<>());
        }
        this.cards = new ArrayList<>();
    }

    /**
     * questo metodo viene chiamato dal server per inizializzare
     * l'indirizzo multicast del progetto durante la fase di deserializzazione
     * non può essere chiamato da nessun altro
     *
     */
    public void initChatAddress(String address) throws AlreadyInitialedException {
        if (this.chatAddress != null)
            throw new AlreadyInitialedException();
        this.chatAddress = address;
    }

    /**
     * questo metodo viene chiamato dal server per inizializzare
     * l'indirizzo multicast del progetto durante la fase di deserializzazione
     * non può essere chiamato da nessun altro
     *
     */
    public void initChatPort(int port) {
        this.chatPort = port;
    }

    /**
     * questo metodo viene chiamato dal server per inizializzare
     * la lista delle cards precedentemente caricata
     * non può essere chiamato da nessun altro
     *
     */
    public void initCardList(List<Card> cards) throws AlreadyInitialedException {
        if (this.cards != null)
            throw new AlreadyInitialedException();
        this.cards = cards;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public LocalDateTime getCreationDateTime() {
        return this.creationDateTime;
    }

    public String getChatAddress() {
        return this.chatAddress;
    }

    public int getChatPort() {
        return this.chatPort;
    }

    public Map<CardStatus, List<String>> getStatusLists() {
        return this.statusLists;
    }

    public Card getCard(String cardName) throws CardNotExistsException {
        Card temp = new Card(cardName, "");
        int index = this.cards.indexOf(temp);
        if (index == -1)
            throw new CardNotExistsException();
        return this.cards.get(index);
    }

    @JsonIgnore
    public List<Card> getAllCards() {
        return this.cards;
    }

    public List<Card> getCardList(CardStatus status) {
        List<Card> toReturn = new ArrayList<>();
        for (Card card : this.cards) {
            if (statusLists.get(status).contains(card.getName())) {
                toReturn.add(card);
            }
        }
        return toReturn;
    }

    public void moveCard (String cardName, CardStatus from, CardStatus to)
            throws OperationNotAllowedException, CardNotExistsException {
        // verifico che la mossa sia consentita
        if (!this.moveIsAllowed(from, to))
            throw new OperationNotAllowedException();

        // verifico che la carta esista
        Card temp = new Card(cardName, "");
        if (!this.cards.contains(temp))
            throw new CardNotExistsException();

        List<String> fromList = this.statusLists.get(from);
        List<String> toList = this.statusLists.get(to);
        if (!fromList.contains(cardName))
            throw new OperationNotAllowedException();

        fromList.remove(cardName);
        toList.add(cardName);

        // aggiungo movimento alla card
        Card thisCard = this.cards.get(this.cards.indexOf(temp));
        thisCard.changeStatus(to);
    }

    public void addCard(Card card) throws CardAlreadyExistsException {
        if (this.cards.contains(card))
            throw new CardAlreadyExistsException();
        this.cards.add(card);
        this.statusLists.get(CardStatus.TODO).add(card.getName());
    }

    public void addMember(String user) throws UserAlreadyPresentException {
        if (this.members.contains(user))
            throw new UserAlreadyPresentException();
        this.members.add(user);
    }

    // posso eliminare il progetto?
    @JsonIgnore
    public boolean isCancelable() {
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            if (status != CardStatus.DONE)
                if (!this.statusLists.get(status).isEmpty())
                    return false;
        }
        return true;
    }

    // posso fare tale spostamento?
    private boolean moveIsAllowed(CardStatus from, CardStatus to) {
        if (from == to) return false;
        // da _todo posso andare solo in inprogress
        if (from == CardStatus.TODO && (to != CardStatus.INPROGRESS))
            return false;
        // da inprogress non posso andare in _todo
        if (from == CardStatus.INPROGRESS && (to == CardStatus.TODO))
            return false;
        // da toberevised non posso andare in _todo
        if (from == CardStatus.TOBEREVISED && (to == CardStatus.TODO))
            return false;
        // da done non posso andare da nessuna parte
        if (from == CardStatus.DONE)
            return false;
        return true;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.name.equals(((Project)o).getName());
    }

}