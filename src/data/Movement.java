package worth.data;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Rappresenta un movimento di una carta
 */
public class Movement implements Serializable {
    private CardStatus from;
    private CardStatus to;
    private LocalDateTime when;

    @JsonCreator
    private Movement() {}

    public Movement(CardStatus from, CardStatus to) {
        this.from = from;
        this.to = to;
        this.when = LocalDateTime.now(ZoneId.systemDefault());
    }
}
