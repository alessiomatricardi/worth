package worth;

import worth.server.PersistentData;
import worth.server.RMITask;
import worth.server.SelectionTask;
import worth.server.rmi.RMICallbackServiceImpl;

import java.io.IOException;
import java.rmi.RemoteException;

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

        // creo oggetto per callback
        RMICallbackServiceImpl callbackService;
        try {
            callbackService = new RMICallbackServiceImpl();
        } catch (RemoteException e) {
            System.out.println("RMI callback service initialization failed. Stack Trace: ");
            e.printStackTrace();
            return;
        }

        // run task registrazione utenti
        RMITask registrationTask = new RMITask(data, callbackService);
        new Thread(registrationTask).start();

        // gestione connessioni TCP
        SelectionTask selectionTask = new SelectionTask(data, callbackService);
        new Thread(selectionTask).start();

    }

}
