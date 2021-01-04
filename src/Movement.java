package worth;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Rappresenta un movimento di una carta
 */
public class Movement implements Serializable {
    private final CardStatus from;
    private final CardStatus to;
    private final LocalDateTime when;

    public Movement(CardStatus from, CardStatus to) {
        this.from = from;
        this.to = to;
        this.when = LocalDateTime.now(ZoneId.systemDefault());
    }
}
