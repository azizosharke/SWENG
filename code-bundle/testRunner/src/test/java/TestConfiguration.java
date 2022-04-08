import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Test;
import rapid7.configuration.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfiguration {
    @Test
    public void testConstructors() throws IOException {
        new Configuration(new FileInputStream("configurations/example_config.json"));
    }

    @Test
    public void testConstructorEquality() throws IOException {
        assertEquals(
                new Configuration(new FileInputStream("configurations/example_config.json")),
                new Configuration("configurations/example_config.json"),
                "Configuration constructor for input stream and file name differs");
    }

    @Test
    public void testInvalidJsonInputs() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                new Configuration( Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        "negative_exec_count.json"))));
        assertTrue(exception.getMessage().contains("cannot be negative or 0"));

        exception = assertThrows(RuntimeException.class, () ->
                new Configuration(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        "zero_exec_count.json"))));
        assertTrue(exception.getMessage().contains("cannot be negative or 0"));

        assertThrows(JsonParseException.class, () -> new Configuration(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        "invalid_exec_count.json"))));

        exception = assertThrows(RuntimeException.class, () ->
                new Configuration(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        "invalid_search_file.json"))));
        assertTrue(exception.getMessage().contains("property not found or null"));

        exception = assertThrows(RuntimeException.class, () ->
                new Configuration(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        "invalid_search_text.json"))));
        assertTrue(exception.getMessage().contains("property not found or null"));
    }
}
