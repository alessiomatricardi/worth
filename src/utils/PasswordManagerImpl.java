package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class PasswordManagerImpl implements PasswordManager {
    private static final Random RANDOM = new SecureRandom();
    private static final int SALT_SIZE = 64;
    private static final String ALGORITHM = "SHA3-256";

    @Override
    public String getSalt() {
        byte[] salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    @Override
    public String hash(String password, String salt) {
        String digest = "";
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            final byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            final byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
            final byte[] allToBeHashed = Utils.concat(passwordBytes, saltBytes);
            final byte[] hashBytes = messageDigest.digest(allToBeHashed);
            digest = bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException ignored) {}
        return digest;
    }

    @Override
    public boolean isExpectedPassword(String password, String salt, String hash) {
        String generatedHash = hash(password, salt);
        return hash.equals(generatedHash);
    }

    /**
     * @param hash bytes da convertire in formato esadecimale
     *
     * @return stringa Hex corrispondente ai bytes
     * */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
