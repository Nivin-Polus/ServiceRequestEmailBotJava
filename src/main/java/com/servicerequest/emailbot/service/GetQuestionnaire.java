package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class GetQuestionnaire {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String QUESTIONNAIRE_API = dotenv.get("QUESTIONNAIRE_API");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<Map.Entry<Map<String, Object>, String>> getQuestionnaire(
            String serviceRequestId, String emailId) {

        Map<String, Object> session = AuthService.getServiceTrackerSession(emailId); 
        if (session == null || !session.containsKey("cookie_token")) {
            return CompletableFuture.completedFuture(
                Map.entry(null, "❌ No valid session found for user " + emailId + ". Please log in again."));
        }

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(QUESTIONNAIRE_API))
                .header("Content-Type", "application/json")
                .header("Cookie", "Cookie_Token=" + session.get("cookie_token"))
                .GET();

        HttpClient client = HttpClient.newHttpClient();
        
        return client.sendAsync(reqBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() != 200) {
                            return Map.entry(null, "❌ Failed to fetch questionnaire: HTTP " + response.statusCode());
                        }

                        Map<String, Object> data = mapper.readValue(response.body(), 
                                new TypeReference<Map<String, Object>>() {});
                        
                        return Map.entry(data, null);
                        
                    } catch (Exception e) {
                        return Map.entry(null, "❌ Error parsing questionnaire response: " + e.getMessage());
                    }
                });
    }

    public static Map<String, Object> buildQuestionnaireData(String serviceRequestId, String emailId, 
            Map<String, Object> questionnaireTemplate) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("service_request_id", serviceRequestId);
        result.put("email_id", emailId);
        result.put("template", questionnaireTemplate);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
