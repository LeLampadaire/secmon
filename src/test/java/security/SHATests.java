package security;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SHATests {
    @Test
    public void testBasic() {
        String message = "Bonjour, je suis un message qui va être hashé. Bonne journée :)";
        assertNotNull(SHA.hash(message));
        assertEquals(SHA.hash(message),SHA.hash(message));
        System.out.println(SHA.hash(message));
    }
}
