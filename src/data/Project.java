package worth.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import worth.exceptions.*;
import worth.utils.MulticastAddressManager;

import java.io.Serializable;
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
    /**
     * l'indirizzo della chat multicast non viene mai serializzato/deserializzato
     * quando il progetto viene recuperato dal server, gli viene assegnato un nuovo indirizzo
     */
    @JsonIgnore
    private String chatAddress;
    private Map<CardStatus, List<String>> statuslists; // 4 liste
    /**
     * le card del progetto vengono serializzate in un file per ognuna
     */
    @JsonIgnore
    private List<Card> cards;

    @JsonCreator
    public Project() {}

    public Project(String projectName) throws NoSuchAddressException {
        this.name = projectName;
        this.members = new ArrayList<>();
        this.chatAddress = MulticastAddressManager.getAddress();
        this.statuslists = new HashMap<>();
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            this.statuslists.put(status, new ArrayList<>());
        }
        this.cards = new ArrayList<>();
    }

    /**
     * questo metodo viene chiamato dal server per inizializzare
     * l'indirizzo multicast del progetto durante la fase di deserializzazione
     * non può essere chiamato da nessun altro
     *
     */
    public void initChatAddress(String address) throws AlreadyInitializedException {
        if (this.chatAddress != null)
            throw new AlreadyInitializedException();
        this.chatAddress = address;
    }

    /**
     * questo metodo viene chiamato dal server per inizializzare
     * la lista delle cards precedentemente caricata
     * non può essere chiamato da nessun altro
     *
     */
    public void initCardList(List<Card> cards) throws AlreadyInitializedException {
        if (this.cards != null)
            throw new AlreadyInitializedException();
        this.cards = cards;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public String getChatAddress() {
        return this.chatAddress;
    }

    public List<Card> getAllCards() {
        return this.cards;
    }

    public List<Card> getStatusList(CardStatus status) {
        List<Card> toReturn = new ArrayList<>();
        for (Card card : this.cards) {
            if (statuslists.get(status).contains(card.getName())) {
                toReturn.add(card);
            }
        }
        return toReturn;
    }

    public void moveCard (String cardName, CardStatus from, CardStatus to)
            throws OperationNotAllowedException, CardNotExistsException {
        // todo serializza nuovo movimento e progetto
        if (!this.moveIsAllowed(from, to))
            throw new OperationNotAllowedException();

        List<String> fromList = this.statuslists.get(from);
        List<String> toList = this.statuslists.get(to);
        if (!fromList.contains(cardName))
            throw new CardNotExistsException();

        fromList.remove(cardName);
        toList.add(cardName);

        // aggiungo movimento alla card
        Card thisCard = this.cards.get(this.cards.indexOf(new Card(cardName, "")));
        thisCard.addMovement(new Movement(from, to));
    }

    public void addCard(Card card) throws AlreadyExistsCardException {
        if (this.cards.contains(card))
            throw new AlreadyExistsCardException();
        this.cards.add(card);
        this.statuslists.get(CardStatus.TODO).add(card.getName());
    }

    public void addMember(String user) {
        if (this.members.contains(user))
            // todo throw
            return;
        this.members.add(user);
    }

    // posso eliminare il progetto?
    public boolean canBeClosed() {
        CardStatus[] values = CardStatus.values();
        for (CardStatus status : values) {
            if (status != CardStatus.DONE)
                if (!this.statuslists.get(status).isEmpty())
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