package org.local;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.client.HealthBEServiceJavaClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class HealthBEIntegrationTestSuite {

    private final HealthBEServiceJavaClient client = new HealthBEServiceJavaClient("http://localhost:8080");

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    @Test
    void upload_happyPath_returns202WithJobId() throws Exception {
        String content = readBase64FromFile("DogFur.txt");
        HttpResponse<String> response = client.uploadImageWithAnimalType("DogFur.jpg", content, "DOG");

        assertEquals(202, response.statusCode());
        assertTrue(response.body().contains("jobId"));
        assertTrue(response.body().contains("ACCEPTED"));
    }

    @Test
    void upload_missingFilename_returns400() throws Exception {
        HttpResponse<String> response = client.uploadImageRaw("""
            { "content": "dGVzdA==" }
        """);

        assertEquals(400, response.statusCode());
    }

    @Test
    void upload_missingContent_returns400() throws Exception {
        HttpResponse<String> response = client.uploadImageRaw("""
            { "filename": "scan.jpg" }
        """);

        assertEquals(400, response.statusCode());
    }

    // -------------------------------------------------------------------------
    // Presign
    // -------------------------------------------------------------------------

    @Test
    void presign_happyPath_returnsUploadUrlAndKey() throws Exception {
        HttpResponse<String> response = client.presign("scan.jpg");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("uploadUrl"));
        assertTrue(response.body().contains("key"));
    }

    @Test
    void presign_missingFilename_returns400() throws Exception {
        HttpResponse<String> response = client.presignNoParam();
        assertEquals(400, response.statusCode());
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    @Test
    void query_missingJobId_returns400() throws Exception {
        HttpResponse<String> response = client.queryJobNoParam();
        assertEquals(400, response.statusCode());
    }

    @Test
    void query_nonExistentJobId_returns200WithNullOrEmpty() throws Exception {
        HttpResponse<String> response = client.queryJob("0000000000000000000000000000000000000000000000000000000000000001");
        // Either 200 with empty/null result or 404 depending on implementation
        assertTrue(response.statusCode() == 200 || response.statusCode() == 404);
    }

    @Test
    void query_validJobId_returns200() throws Exception {
        // Upload first to get a real jobId
        String content = readBase64FromFile("DogFur.txt");
        HttpResponse<String> uploadResponse = client.uploadImageWithAnimalType("DogFur.jpg", content, "DOG");
        assertEquals(202, uploadResponse.statusCode());

        String jobId = extractJobId(uploadResponse.body());
        assertNotNull(jobId);

        HttpResponse<String> queryResponse = client.queryJob(jobId);
        assertEquals(200, queryResponse.statusCode());
        assertTrue(queryResponse.body().contains("status"));
    }

    // -------------------------------------------------------------------------
    // Download
    // -------------------------------------------------------------------------

    @Test
    void download_missingObjectKey_returns400() throws Exception {
        HttpResponse<String> response = client.downloadNoParam();
        assertEquals(400, response.statusCode());
    }

    @Test
    void download_nonExistentKey_returns500() throws Exception {
        HttpResponse<byte[]> response = client.download("nonexistent/key.pdf");
        assertEquals(500, response.statusCode());
    }

    // -------------------------------------------------------------------------
    // Feedback
    // -------------------------------------------------------------------------

    @Test
    void feedback_happyPath_returns201() throws Exception {
        HttpResponse<String> response = client.submitFeedback("deadbeefdeadbeefdeadbeefdeadbeef", 5, "Great results");
        assertEquals(201, response.statusCode());
    }

    @Test
    void feedback_missingJobId_returns400() throws Exception {
        HttpResponse<String> response = client.submitFeedbackRaw("""
            { "starRating": 4 }
        """);
        assertEquals(400, response.statusCode());
    }


    // -------------------------------------------------------------------------
    // Survey
    // -------------------------------------------------------------------------

    @Test
    void survey_happyPath_validJobId_returns200() throws Exception {
        String content = readBase64FromFile("DogFur.txt");
        HttpResponse<String> uploadResponse = client.uploadImageWithAnimalType("DogFur.jpg", content, "DOG");
        assertEquals(202, uploadResponse.statusCode());

        String jobId = extractJobId(uploadResponse.body());
        assertNotNull(jobId);

        HttpResponse<String> surveyResponse = client.submitSurvey(
                jobId, "adult", "less_than_24h", "normal_active", "pink_normal");
        assertEquals(200, surveyResponse.statusCode());
    }

    @Test
    void survey_invalidJobIdFormat_returns400() throws Exception {
        HttpResponse<String> response = client.submitSurvey(
                "not-a-valid-id!", "adult", "less_than_24h", "normal_active", "pink_normal");
        assertEquals(400, response.statusCode());
    }

    @Test
    void survey_nonExistentJobId_returns404() throws Exception {
        String ghostJobId = "0000000000000000000000000000000000000000000000000000000000000002";
        HttpResponse<String> response = client.submitSurvey(
                ghostJobId, "adult", "less_than_24h", "normal_active", "pink_normal");
        assertEquals(404, response.statusCode());
    }

    @Test
    void survey_invalidAnswerValues_returns400() throws Exception {
        String content = readBase64FromFile("DogFur.txt");
        HttpResponse<String> uploadResponse = client.uploadImageWithAnimalType("DogFur.jpg", content, "DOG");
        assertEquals(202, uploadResponse.statusCode());

        String jobId = extractJobId(uploadResponse.body());
        assertNotNull(jobId);

        HttpResponse<String> response = client.submitSurveyRaw(jobId, """
            {
                "age_group": "INVALID_VALUE",
                "symptom_duration": "less_than_24h",
                "energy_level": "normal_active",
                "gum_color": "pink_normal"
            }
        """);
        assertEquals(400, response.statusCode());
    }

    // -------------------------------------------------------------------------
    // Text-Only Survey
    // -------------------------------------------------------------------------

    @Test
    void textOnlySurvey_happyPath_returns200WithAllFields() throws Exception {
        HttpResponse<String> response = client.submitTextOnlySurvey(
                "adult", "less_than_24h", "normal_active", "pink_normal");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("jobId"));
        assertTrue(response.body().contains("triageTier"));
        assertTrue(response.body().contains("summary"));
    }

    @Test
    void textOnlySurvey_lowRiskAnswers_returnsTierLow() throws Exception {
        HttpResponse<String> response = client.submitTextOnlySurvey(
                "adult", "less_than_24h", "normal_active", "pink_normal");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("LOW"));
    }

    @Test
    void textOnlySurvey_shortCircuitAnswers_returnsTierHigh() throws Exception {
        HttpResponse<String> response = client.submitTextOnlySurvey(
                "adult", "less_than_24h", "unresponsive", "pink_normal");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("HIGH"));
    }

    @Test
    void textOnlySurvey_invalidAnswerValue_returns400() throws Exception {
        HttpResponse<String> response = client.submitTextOnlySurveyRaw("""
            {
                "age_group": "INVALID_VALUE",
                "symptom_duration": "less_than_24h",
                "energy_level": "normal_active",
                "gum_color": "pink_normal"
            }
        """);
        assertEquals(400, response.statusCode());
    }

    @Test
    void textOnlySurvey_missingField_returns400() throws Exception {
        HttpResponse<String> response = client.submitTextOnlySurveyRaw("""
            {
                "age_group": "adult",
                "symptom_duration": "less_than_24h",
                "energy_level": "normal_active"
            }
        """);
        assertEquals(400, response.statusCode());
    }

    @Test
    void textOnlySurvey_jobIdIsUniquePerRequest() throws Exception {
        HttpResponse<String> r1 = client.submitTextOnlySurvey(
                "adult", "less_than_24h", "normal_active", "pink_normal");
        HttpResponse<String> r2 = client.submitTextOnlySurvey(
                "adult", "less_than_24h", "normal_active", "pink_normal");

        assertEquals(200, r1.statusCode());
        assertEquals(200, r2.statusCode());

        String jobId1 = extractJobId(r1.body());
        String jobId2 = extractJobId(r2.body());
        assertNotNull(jobId1);
        assertNotNull(jobId2);
        assertNotEquals(jobId1, jobId2);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String readBase64FromFile(String filename) throws Exception {
        Path path = Path.of("src", "test", "resources", filename);
        return Files.readString(path).trim();
    }

    private static String extractJobId(String responseBody) {
        // Simple extraction from JSON: "jobId":"<value>"
        int idx = responseBody.indexOf("\"jobId\"");
        if (idx == -1) return null;
        int start = responseBody.indexOf("\"", idx + 7) + 1;
        int end = responseBody.indexOf("\"", start);
        return responseBody.substring(start, end);
    }
}
