package org;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.client.HealthBEServiceJavaClient;
import org.junit.jupiter.api.Test;

public class HealthBEIntegrationTestSuite {

    private final HealthBEServiceJavaClient client = new HealthBEServiceJavaClient("http://localhost:8080");

    @Test
    void testUpload() throws Exception {
        String filename = "BasalCell.txt";
        String content = readBase64FromFile(filename);

        String response = client.uploadImage(filename, content);

        assertNotNull(response);
        System.out.println(response);
        assertTrue(response.contains("ACCEPTED"));
    }

    private static String readBase64FromFile(String filename) throws Exception {
        Path path = Path.of("src", "test", "resources", filename);
        return Files.readString(path).trim();
    }

}
