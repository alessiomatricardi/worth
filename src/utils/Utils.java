package worth.utils;

import javax.swing.JOptionPane;

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

    /**
     * @param message messaggio da stampare a video
     *
     * @return from JOptionPane.showOptionDialog method:
     * "an integer indicating the option chosen by the user,
     * or CLOSED_OPTION if the user closed the dialog"
     */
    public static int showErrorMessageDialog(String message) {
        return JOptionPane.showOptionDialog(
                null,
                message,
                "Error",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                null,
                null
        );
    }

    /**
     * @param message messaggio da stampare a video
     *
     * @return from JOptionPane.showOptionDialog method:
     * "an integer indicating the option chosen by the user,
     * or CLOSED_OPTION if the user closed the dialog"
     */
    public static int showInfoMessageDialog(String message) {
        return JOptionPane.showOptionDialog(
                null,
                message,
                "Info",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                null
        );
    }

    /**
     * @param message messaggio da stampare a video
     *
     * @return from JOptionPane.showOptionDialog method:
     * "an integer indicating the option chosen by the user,
     * or CLOSED_OPTION if the user closed the dialog"
     */
    public static int showQuestionMessageDialog(String message) {
        return JOptionPane.showOptionDialog(
                null,
                message,
                "Worth",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null
        );
    }

}
