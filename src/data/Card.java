package worth.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class Card implements Serializable {
    private String name;
    private String description;
    private CardStatus status;
    private List<Movement> movements;

    public Card(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = CardStatus.TODO;
        this.movements = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public CardStatus getStatus() {
        return this.status;
    }

    public List<Movement> getMovements() {
        return this.movements;
    }

    public void changeStatus(CardStatus newStatus) {
        Movement mov = new Movement(this.status, newStatus);
        this.status = newStatus;
        this.movements.add(mov);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.name.equals(((Card)o).getName());
    }
}
