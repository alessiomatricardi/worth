package worth.client.model.rmi;

import worth.data.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by alessiomatricardi on 05/01/21
 */
public interface RMICallbackNotify extends Remote { // todo interface

    void notifyUpdate(String username, UserStatus status) throws RemoteException;

}
