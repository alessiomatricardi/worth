package worth;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class Card implements Serializable {
    private String name;
    private String description;
    private List<Movement> movements;

    public Card(String name, String description) {
        this.name = name;
        this.description = description;
        this.movements = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Movement> getMovements() {
        return this.movements;
    }

    public void addMovement(Movement mov) {
        movements.add(mov);
    }
}

class Movement implements Serializable {
    private final CardStatus from;
    private final CardStatus to;
    private final LocalDateTime when;

    public Movement(CardStatus from, CardStatus to) {
        this.from = from;
        this.to = to;
        this.when = LocalDateTime.now(ZoneId.systemDefault());
    }
}

enum CardStatus implements Serializable {
    TODO,
    INPROGRESS,
    TOBEREVISED,
    DONE
}
