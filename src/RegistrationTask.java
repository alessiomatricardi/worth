package worth;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by alessiomatricardi on 02/01/21
 *
 * Task che si occupa della registrazione degli utenti
 */
public class RegistrationTask implements Runnable {
    public static final int REGISTRY_PORT = 6789;
    private final Registration registration;

    public RegistrationTask(Registration registration) {
        this.registration = registration;
    }

    @Override
    public void run() {
        try {
            // creo oggetto
            RegistrationService registrationService = new RegistrationServiceImpl(registration);

            // creo registry su porta REG_PORT
            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);

            // pubblico stub nel registry
            registry.rebind(RegistrationService.REGISTRATION_SERVICE_NAME, registrationService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
