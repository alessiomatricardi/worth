import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class Card {
    private String name;
    private String description;
    private List<Movement> movements;
}

class Movement {
    CardStatus from;
    CardStatus to;
    LocalDateTime when;

    public Movement(CardStatus from, CardStatus to) {
        this.from = from;
        this.to = to;
        this.when = LocalDateTime.now(ZoneId.systemDefault());
    }
}

enum CardStatus {
    TODO,
    INPROGRESS,
    TOBEREVISED,
    DONE
}
