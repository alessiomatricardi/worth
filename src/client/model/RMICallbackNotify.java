package worth.client.model;

import worth.RegisteredUser;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by alessiomatricardi on 05/01/21
 */
public interface RMICallbackNotify extends Remote {

    void notifyUpdate(List<RegisteredUser> users) throws RemoteException;

}
