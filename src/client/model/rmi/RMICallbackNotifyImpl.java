package worth.client.model.rmi;

import worth.data.UserStatus;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Implementazione del servizio di callback lato client
 */
public class RMICallbackNotifyImpl extends UnicastRemoteObject implements RMICallbackNotify {
    private final Map<String, UserStatus> userStatus;

    public RMICallbackNotifyImpl(Map<String, UserStatus> userStatus) throws RemoteException {
        super();
        this.userStatus = userStatus;
    }

    @Override
    public synchronized void notifyUpdate(String username, UserStatus status) throws RemoteException {
        this.userStatus.put(username, status);
    }
}
