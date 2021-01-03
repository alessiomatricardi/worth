package worth.utils;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * PasswordManager interface, for SHA
 */
public interface PasswordManager {
    String ALGORITHM = "SHA3-256"; // algoritmo utilizzato

    /**
     * Genera salt di 64 bytes in formato Hex
     * Nota: attualmente la dimensione è 64, ma potrebbe aumentare
     *
     * @return stringa di caratteri contenenti il salt
     */
    String getSalt();

    /**
     * Genera l'hash di una password attraverso l'algoritmo SHA3-256
     *
     * @param password password in chiaro
     * @param salt salt generato casualmente
     * @return hash della password, in base Hex
     */
    String hash(final String password, final String salt);

    /**
     * Verifica che la password corrisponda
     *
     * @param password password da verificare
     * @param salt salt utilizzato per generare l'hash della password (persistente)
     * @param hash hash generato (persistente)
     * @return true se la password corrisponde, false altrimenti
     */
    boolean isExpectedPassword(final String password, final String salt, final String hash);
}
