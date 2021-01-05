package worth.server;

import worth.client.model.RMICallbackNotify;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * todo specifica
 */
public interface RMICallbackService extends Remote {

    void registerForCallback(RMICallbackNotify client) throws RemoteException;

    void unregisterForCallback(RMICallbackNotify client) throws RemoteException;

}
