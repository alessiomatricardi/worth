package worth;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class ServerMain {

    public static void main (String[] args) {

        // Caricamento di tutti i dati da locale
        PersistentData data = PersistentData.init();

        // run task registrazione utenti
        RegistrationTask registrationTask = new RegistrationTask(data);
        new Thread(registrationTask).start();

        // callback RMI

        // gestione connessioni TCP
        SelectionTask selectionTask = new SelectionTask();
        new Thread(selectionTask).start();

    }

}
