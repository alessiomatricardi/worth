package worth;

import worth.exceptions.UsernameNotAvailableException;


/**
 * Created by alessiomatricardi on 02/01/21
 */
public interface Registration {

    /**
     * Registrazione di utente su WORTH
     *
     * @param username username dell'utente
     * @param hash hash della password dell'utente
     *
     * @throws UsernameNotAvailableException se l'username non è disponibile
     */
    void registerUser(String username, String hash) throws UsernameNotAvailableException;

}
