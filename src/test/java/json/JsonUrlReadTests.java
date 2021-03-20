package json;

import org.junit.Before;
import org.junit.Test;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JsonUrlReadTests {
    Map<String, Object> keyManager;
    public static final int GCM_IV_LENGTH = 12;
    public static byte[] VECTOR = null;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        keyManager = new HashMap<>();
        keyManager.put("key", new SecretKeySpec("E(H+MbQeThWmZq4t7w9z$C&F)J@NcRfU".getBytes(),"AES"));
        VECTOR = new byte[GCM_IV_LENGTH];
        keyManager.put("vector", VECTOR);
        SecureRandom random = new SecureRandom();
        keyManager.put("random", random);
        random.nextBytes((byte[]) keyManager.get("vector"));
    }

    @Test
    public void testAJsonFile() {
        final JsonUrlRead reader = new JsonUrlRead();
        final String[] expectedHttps = {"test https"};
        final String[] expectedSnmps = {"test snmp"};

        /*assertEquals(expectedHttps[0], reader.readHttps()[0]);
        assertEquals(1, reader.readHttps().length);
        assertEquals(expectedSnmps[0], reader.readSnmp()[0]);
        assertEquals(1, reader.readSnmp().length);*/

        for (String url :reader.readHttps()) {
            System.out.println(url);
        }

        for (String url :reader.readSnmp()) {
            System.out.println(url);
        }
    }

    @Test
    public void testWriteAJson() {
        /*final Client[] temp = {new Client("Nathan","123"),new Client("Julien","456")};
        final List<Client> users = Arrays.asList(temp) ;
        String[] result = Json.readKeyAndVector(Paths.get("src", "test","resources", "key.json").toAbsolutePath().toString());
        Json.write(users,Paths.get("src", "test","resources", "bd.json").toAbsolutePath().toString(), new SecretKeySpec(result[0].getBytes(),"AES"), result[1].getBytes());

        //Verify the content
        Json.readUsers(Paths.get("src", "test","resources", "accounts.json").toAbsolutePath().toString(), new SecretKeySpec(result[0].getBytes(),"AES"), result[1].getBytes());
        /*assertEquals(users.get(0).getLogin(), expected.get(0).getLogin());
        System.out.println(users.get(0).getLogin() + " " + expected.get(0).getLogin());
        assertNotEquals(users.get(0).getPass(), expected.get(0).getPass());
        System.out.println(users.get(0).getPass() + " " + expected.get(0).getPass());
        assertEquals(users.get(1).getLogin(), expected.get(1).getLogin());
        System.out.println(users.get(1).getLogin() + " " + expected.get(1).getLogin());
        assertNotEquals(users.get(1).getPass(), expected.get(1).getPass());
        System.out.println(users.get(1).getPass() + " " + expected.get(1).getPass());*/
    }

    @Test
    public void testWriteANewUrl() {
        String url = "testNewUrl";

        JsonWriterUrl.writeANewUrl("https",url);
    }
}
