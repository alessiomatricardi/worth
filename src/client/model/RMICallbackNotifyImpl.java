package worth.client.model;

import worth.RegisteredUser;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.List;

/**
 * Created by alessiomatricardi on 05/01/21
 */
public class RMICallbackNotifyImpl extends RemoteObject implements RMICallbackNotify {
    private List<RegisteredUser> users;

    public RMICallbackNotifyImpl(List<RegisteredUser> users) {
        this.users = users;
    }

    @Override
    public void notifyUpdate(List<RegisteredUser> updatedUsers) throws RemoteException {
        this.users = updatedUsers;
    }
}
