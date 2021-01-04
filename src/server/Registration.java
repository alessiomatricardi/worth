package worth.server;

import worth.exceptions.UsernameNotAvailableException;


/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Interfaccia resa disponibile in esclusiva al servizio RMI per questioni di information hiding
 * limita l'accesso ai dati del server
 */
public interface Registration {

    /**
     * Registrazione di utente su WORTH
     *
     * @param username username dell'utente
     * @param hash hash della password dell'utente
     * @param salt utilizzato per generare la password
     *
     * @throws UsernameNotAvailableException se l'username non è disponibile
     */
    void registerUser(String username, String hash, String salt) throws UsernameNotAvailableException;

}
