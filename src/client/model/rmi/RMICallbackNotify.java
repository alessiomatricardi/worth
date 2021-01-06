package worth.client.model.rmi;

import worth.data.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Interfaccia lato client del servizio RMI di callback
 */
public interface RMICallbackNotify extends Remote {

    /**
     * @param username nome utente su cui ricevere notifica
     * @param status nuovo stato dell'utente
     *
     * @throws RemoteException se ci sono errori con il servizio rmi
     * */
    void notifyUpdate(String username, UserStatus status) throws RemoteException;

}
