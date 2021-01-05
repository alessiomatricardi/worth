package worth.server;

import worth.protocol.CommunicationProtocol;
import worth.server.rmi.RMICallbackService;
import worth.server.rmi.RMICallbackServiceImpl;
import worth.server.rmi.RMIRegistrationService;
import worth.server.rmi.RMIRegistrationServiceImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Task che si occupa della registrazione degli utenti
 */
public class RMITask implements Runnable {
    private final Registration registration;
    private final RMICallbackServiceImpl callbackService;

    public RMITask(Registration registration, RMICallbackServiceImpl callbackService) {
        this.registration = registration;
        this.callbackService = callbackService;
    }

    @Override
    public void run() {
        try {
            // creo registry su porta REG_PORT
            Registry registry = LocateRegistry.createRegistry(CommunicationProtocol.REGISTRY_PORT);

            // pubblico stub del callback nel registry
            registry.rebind(CommunicationProtocol.CALLBACK_SERVICE_NAME, callbackService);
            System.out.println("Callback service is now available");

            // creo oggetto
            RMIRegistrationService registrationService = new RMIRegistrationServiceImpl(registration, callbackService);

            // pubblico stub della registrazione nel registry
            registry.rebind(CommunicationProtocol.REGISTRATION_SERVICE_NAME, registrationService);
            System.out.println("Registration service is now available");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
