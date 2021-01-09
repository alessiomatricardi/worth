package worth.server.rmi;

import worth.data.UserStatus;
import worth.client.model.rmi.RMICallbackNotify;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Implementazione del servizio di callback lato server
 */
public class RMICallbackServiceImpl extends UnicastRemoteObject implements RMICallbackService {
    List<RMICallbackNotify> clients;
    /*
     * quando un client viene interrotto bruscamente non farà mai la unregister
     * dal servizio rmi e, al momento di invio di una notifica
     * il server riporta errori di connessione col suddetto client
     * per tale ragione, se ci sono errori dovuti alla notifica
     * il client verrà direttamente de-registrato dal servizio di callback
     */
    List<RMICallbackNotify> toDelete;

    public RMICallbackServiceImpl() throws RemoteException {
        clients = new ArrayList<>();
        toDelete = new ArrayList<>();
    }

    @Override
    public synchronized void registerForCallback(RMICallbackNotify client) throws RemoteException {
        if (!clients.contains(client)) {
            clients.add(client);
        }
    }

    @Override
    public synchronized void unregisterForCallback(RMICallbackNotify client) throws RemoteException {
        clients.remove(client);
    }

    /**
     * @param username nome utente da notificare ai client
     * @param status status dell'utente da notificare
     *
     */
    public void notifyUsers(String username, UserStatus status) {
        doCallbacks(username, status);
    }

    private synchronized void doCallbacks(String username, UserStatus status) {
        for (RMICallbackNotify client : clients) {
            try {
                client.notifyUpdate(username, status);
            } catch (RemoteException e) {
                toDelete.add(client);
            }
        }
        while (!toDelete.isEmpty()) {
            clients.remove(toDelete.remove(0));
        }
    }

}
