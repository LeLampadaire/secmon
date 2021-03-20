package security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {
    public static String hash(String message) {
        String texthash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            byte[] messagedigest = md.digest(message.getBytes());

            BigInteger integer = new BigInteger(1,messagedigest);

             texthash = integer.toString(16);

            while (texthash.length() < 32) {
                texthash = "0" + texthash;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return texthash;
     }
}
