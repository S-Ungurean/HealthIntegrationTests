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

    public HttpResponse<String> uploadImage(String filename, String contentBase64) throws Exception {
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

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> uploadImageWithAnimalType(String filename, String contentBase64, String animalType) throws Exception {
        String jsonBody = String.format("""
            {
                "filename": "%s",
                "content": "%s",
                "animalType": "%s"
            }
        """, filename, contentBase64, animalType);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/upload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> uploadImageRaw(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/upload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> queryJob(String jobId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/query?jobId=" + jobId))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> queryJobNoParam() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/query"))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> presign(String filename) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/presign?filename=" + filename))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> presignNoParam() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/presign"))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<byte[]> download(String objectKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/download?objectKey=" + objectKey))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public HttpResponse<String> downloadNoParam() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/download"))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> submitFeedback(String jobId, int starRating, String comments) throws Exception {
        String jsonBody = String.format("""
            {
                "jobId": "%s",
                "starRating": %d,
                "comments": "%s"
            }
        """, jobId, starRating, comments);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/feedback"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> submitFeedbackRaw(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/image/feedback"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
