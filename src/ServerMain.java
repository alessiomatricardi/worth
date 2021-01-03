package worth;

import worth.server.PersistentData;
import worth.server.RegistrationTask;
import worth.server.SelectionTask;

import java.io.IOException;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class ServerMain {

    public static void main (String[] args) {

        // Caricamento di tutti i dati da locale
        PersistentData data;
        try {
            data = new PersistentData();
        } catch (IOException e) {
            System.out.println("Data initialization failed. Stack Trace: ");
            e.printStackTrace();
            return;
        }

        // run task registrazione utenti
        RegistrationTask registrationTask = new RegistrationTask(data);
        new Thread(registrationTask).start();

        // callback RMI

        // gestione connessioni TCP
        SelectionTask selectionTask = new SelectionTask();
        new Thread(selectionTask).start();

    }

}
