package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SubmitQuestionnaire {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String SUBMIT_QUESTIONNAIRE_API = dotenv.get("SUBMIT_QUESTIONNAIRE_API");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<Map.Entry<String, String>> submitQuestionnaire(
            String serviceRequestId, String emailId, Map<String, Object> responses) {

        Map<String, Object> session = AuthService.getServiceTrackerSession(emailId);
        if (session == null || !session.containsKey("cookie_token")) {
            return CompletableFuture.completedFuture(
                Map.entry(null, "❌ No valid session found for user " + emailId));
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("service_request_id", serviceRequestId);
            payload.put("responses", responses);
            payload.put("submitted_by", emailId);
            payload.put("timestamp", System.currentTimeMillis());

            String jsonPayload = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUBMIT_QUESTIONNAIRE_API))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "Cookie_Token=" + session.get("cookie_token"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return Map.entry("✅ Questionnaire submitted successfully", null);
                        } else {
                            return Map.entry(null, "❌ Failed to submit questionnaire: HTTP " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                Map.entry(null, "❌ Error submitting questionnaire: " + e.getMessage()));
        }
    }
}
