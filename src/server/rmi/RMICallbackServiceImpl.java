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
 * todo altri throw??
 */
public class RMICallbackServiceImpl extends UnicastRemoteObject implements RMICallbackService {
    List<RMICallbackNotify> clients;

    public RMICallbackServiceImpl() throws RemoteException {
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

    public void notifyUsers(String username, UserStatus status) throws RemoteException {
        doCallbacks(username, status);
    }

    private synchronized void doCallbacks(String username, UserStatus status) throws RemoteException {
        for (RMICallbackNotify client : clients) {
            client.notifyUpdate(username, status);
        }
    }

}
