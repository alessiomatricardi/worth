package worth.utils;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class Utils {

    /**
     * @param a array di byte
     * @param b array di byte
     *
     * @return concatenazione degli array a e b
     */
    public static byte[] concat (final byte[] a, final byte[] b) {
        byte[] c = new byte[a.length + b.length];
        int k = 0;
        for(byte x : a) {
            c[k++] = x;
        }
        for (byte x : b) {
            c[k++] = x;
        }
        return c;
    }

}
