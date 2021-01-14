package worth.utils;

import worth.exceptions.NoSuchPortException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 14/01/21
 *
 * Gestore di porte per indirizzi multicast dei progetti
 * Genera porte a partire da 30000
 */
public abstract class PortManager {
    private static int port = 30000;
    private static final int MAX_PORT = 65535;
    private static final List<Integer> freePorts = new ArrayList<>();

    public synchronized static int getPort() throws NoSuchPortException {
        if (!freePorts.isEmpty()) {
            return freePorts.remove(0);
        }
        if (port == MAX_PORT)
            throw new NoSuchPortException();
        int toReturn = port;
        port++;
        return toReturn;
    }

    public synchronized static void freePort(int port) {
        if (port < MAX_PORT)
            freePorts.add(port);
    }

}
