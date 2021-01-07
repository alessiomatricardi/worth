package worth.utils;

import worth.exceptions.NoSuchAddressException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiomatricardi on 06/01/21
 *
 * Gestore di indirizzi multicast
 * Genera indirizzi multicast a partire da 239.0.0.0 fino a 239.255.255.255
 */
public abstract class MulticastAddressManager {
    private static final int a1 = 239;
    private static int a2 = 0;
    private static int a3 = 0;
    private static int a4 = 0;
    private static final int MAX_A1 = 239;
    private static final int MAX_A2 = 256;
    private static final int MAX_A3 = 256;
    private static final int MAX_A4 = 256;
    private static final String MAX_ADDRESS = "239.255.255.255";
    private static final List<String> freeAddresses = new ArrayList<>();

    public synchronized static String getAddress() throws NoSuchAddressException {
        if (!freeAddresses.isEmpty()) {
            return freeAddresses.remove(0);
        }
        String addressToSend = a1 + "." + a2 + "." + a3 + "." + a4;
        if (addressToSend.equals(MAX_ADDRESS)) {
            throw new NoSuchAddressException();
        }
        a4++;
        a4 %= MAX_A4;
        if (a4 == 0) {
            a3++;
            a3 %= MAX_A3;
            if (a3 == 0) {
                a2++;
            }
        }

        return addressToSend;
    }

    public synchronized static void freeAddress(String address) {
        if(isValid(address)) {
            freeAddresses.add(address);
        }
    }

    private static boolean isValid(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return inetAddress.isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
