package org.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HealthBEServiceJavaClient {
    private final String baseUrl;
    private final HttpClient client;

    public HealthBEServiceJavaClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
    }

    public String uploadImage(String filename, String contentBase64) throws Exception {
        String jsonBody = String.format("""
            {
                "filename": "%s",
                "content": "%s"
            }
        """, filename, contentBase64);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/upload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
