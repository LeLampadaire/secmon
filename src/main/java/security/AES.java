package security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;
    private final byte[] vector = new byte[GCM_IV_LENGTH];
    private SecretKeySpec key;

    public AES() {
       this.key = new SecretKeySpec(Base64.getDecoder().decode("XODnju7FVYhMrDbMKfz7UUmCg5PLtUJgmWB2kHI9Cx8="),"AES");
    }

    /**
     * Générateur de clé symétrique de cryptage
     *

    /**
     *  KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
     *         keyGenerator.init(AES_KEY_SIZE);
     *
     *         // Generate Key
     *         SecretKey key = keyGenerator.generateKey();
     *         byte[] IV = new byte[GCM_IV_LENGTH];
     *         SecureRandom random = new SecureRandom();
     *         random.nextBytes(IV);
     */

    public byte[] encryptCommunication(final String message) {
        /**
         * vector = nombre arbitraire utilisé avec la clé au moment du cryptage (une seule fois) -> + aléatoire
         * SecureRandom == nombre aléatoire puissant en cryptographie
         */


        try {
            return encryption(message.getBytes());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] encryption(byte[] text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        /**
         * Classe s'occupant du cryptage, décryptage.
         * AES : nom algo
         * GCM : mode d'utilisation de l'algo
         * NoPadding : système de "rembourrage" utilisé
         */
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        //Sous-classe de la classe SecretKey qui construit une SecretKey à partir d'une clé existante
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, vector);

        cipher.init(Cipher.ENCRYPT_MODE, this.key, gcmParameterSpec);

        return cipher.doFinal(text);
    }

    public String decryptCommunication(byte[] cryptText)  {

        /**
         * vector = nombre arbitraire utilisé avec la clé au moment du cryptage (une seule fois) -> + aléatoire
         * SecureRandom == nombre aléatoire puissant en cryptographie
         */
        try {
            return decryption(cryptText,this.vector);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String decryption(byte[] encryptmessage, byte[] vector) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(this.key.getEncoded(), "AES");

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, vector);

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, this.key, gcmParameterSpec);

        // Perform Decryption
        byte[] decryptedText = cipher.doFinal(encryptmessage);

        return new String(decryptedText);
    }
}
