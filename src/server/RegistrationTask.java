package worth.server;

import worth.CommunicationProtocol;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Task che si occupa della registrazione degli utenti
 */
public class RegistrationTask implements Runnable {
    private final Registration registration;

    public RegistrationTask(Registration registration) {
        this.registration = registration;
    }

    @Override
    public void run() {
        try {
            // creo oggetto
            RMIRegistrationService registrationService = new RMIRegistrationServiceImpl(registration);

            // creo registry su porta REG_PORT
            Registry registry = LocateRegistry.createRegistry(CommunicationProtocol.REGISTRY_PORT);

            // pubblico stub nel registry
            registry.rebind(CommunicationProtocol.REGISTRATION_SERVICE_NAME, registrationService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
