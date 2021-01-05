package worth;

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

    public String getName() {
        return this.name;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public String getChatAddress() {
        return this.chatAddress;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.name.equals(((Project)o).getName());
    }

}
