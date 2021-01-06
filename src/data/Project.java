package worth.data;

import worth.exceptions.CardNotExistsException;
import worth.exceptions.OperationNotAllowedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Progetto di Worth
 */
public class Project implements Serializable {
    private String name;
    private List<String> members;
    private String chatAddress;
    private List<Card> todoList;
    private List<Card> inProgressList;
    private List<Card> toBeRevisedList;
    private List<Card> doneList;

    public Project(String projectName) {
        this.name = projectName;
        this.members = new ArrayList<>();
        this.chatAddress = ""; // todo
        this.todoList = new ArrayList<>();
        this.inProgressList = new ArrayList<>();
        this.toBeRevisedList = new ArrayList<>();
        this.doneList = new ArrayList<>();
    }

    public void moveCard (String cardName, CardStatus from, CardStatus to)
            throws OperationNotAllowedException, CardNotExistsException {
        // todo serializza nuovo movimento e progetto
        if (!this.isAllowed(from, to))
            throw new OperationNotAllowedException();
        Card temp = new Card(cardName, "nothing important");
        int index;
        Card card = null;
        if (from == CardStatus.TODO) {
            if ((index = todoList.indexOf(temp)) == -1)
                throw new CardNotExistsException();
            card = todoList.remove(index);
            inProgressList.add(card); // solo inprogress
            card.addMovement(new Movement(from, to));
        } else if (from == CardStatus.INPROGRESS) {
            if ((index = inProgressList.indexOf(temp)) == -1)
                throw new CardNotExistsException();
            card = inProgressList.remove(index);
            if (to == CardStatus.TOBEREVISED) {
                toBeRevisedList.add(card);
            } else {
                doneList.add(card);
            }
            card.addMovement(new Movement(from, to));
        } else if (from == CardStatus.TOBEREVISED) {
            if ((index = toBeRevisedList.indexOf(temp)) == -1)
                throw new CardNotExistsException();
            card = toBeRevisedList.remove(index);
            if (to == CardStatus.INPROGRESS) {
                inProgressList.add(card);
            } else {
                doneList.add(card);
            }
            card.addMovement(new Movement(from, to));
        }
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

    // posso fare tale spostamento?
    private boolean isAllowed(CardStatus from, CardStatus to) {
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