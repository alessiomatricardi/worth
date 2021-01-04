package worth;

import java.io.Serializable;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Rappresenta lo stato di una card
 */
public enum CardStatus implements Serializable {
    TODO,
    INPROGRESS,
    TOBEREVISED,
    DONE
}
