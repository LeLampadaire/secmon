package security;

import org.junit.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class AESTests {

    @Test
    public void testConstantKey() {
        String message = "Bonjour je suis un message, je vais être encodé. :(";
        byte[] crypt = new AES().encryptCommunication(message);

        String returnText = Base64.getEncoder().encodeToString(crypt);

        AES aes2 = new AES();
        assertEquals(message, aes2.decryptCommunication(Base64.getDecoder().decode(returnText)));
    }
}