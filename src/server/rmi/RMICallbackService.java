package worth.server.rmi;

import worth.client.model.rmi.RMICallbackNotify;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Interfaccia lato server del servizio di callback
 */
public interface RMICallbackService extends Remote {

    /**
     * @param client client da registrare al servizio
     *
     * @throws RemoteException se ci sono errori di connessione
     * */
    void registerForCallback(RMICallbackNotify client) throws RemoteException;

    /**
     * @param client client da de-registrare al servizio
     *
     * @throws RemoteException se ci sono errori di connessione
     * */
    void unregisterForCallback(RMICallbackNotify client) throws RemoteException;

}
