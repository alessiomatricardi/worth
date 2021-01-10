package worth.data;

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
    DONE;

    /**
     * Ottieni CardStatus a partire da una stringa
     *
     * @param stringStatus stringa che indica lo stato
     *
     * @return il CardStatus che si riferisce alla stringa, null se non c'è
     */
    public static CardStatus retriveFromString(String stringStatus) {
        for (CardStatus cardStatus : CardStatus.values()) {
            if (cardStatus.name().equals(stringStatus))
                return cardStatus;
        }
        return null;
    }
}
