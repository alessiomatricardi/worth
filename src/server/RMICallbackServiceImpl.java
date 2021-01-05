package worth.server;

import worth.RegisteredUser;
import worth.client.model.RMICallbackNotify;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * todo altri throw??
 */
public class RMICallbackServiceImpl extends UnicastRemoteObject implements RMICallbackService {
    List<RMICallbackNotify> clients;

    protected RMICallbackServiceImpl() throws RemoteException {
        clients = new ArrayList<>();
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

    public void doCallbacks(List<RegisteredUser> updatedUsers) throws RemoteException {
        for (RMICallbackNotify client : clients) {
            client.notifyUpdate(updatedUsers);
        }
    }

}
