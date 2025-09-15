package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SubmitServiceRequest {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String SERVICE_REQUEST_API = dotenv.get("SERVICE_REQUEST_API");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<Map.Entry<String, String>> createServiceRequest(
            String category, String type, String department, String subject, 
            String description, String priority, String sender) {

        Map<String, Object> session = AuthService.getServiceTrackerSession(sender);
        if (session == null || !session.containsKey("cookie_token")) {
            return CompletableFuture.completedFuture(
                Map.entry(null, "❌ No valid session found for user " + sender));
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("category", category);
            payload.put("type", type);
            payload.put("department", department);
            payload.put("subject", subject);
            payload.put("description", description);
            payload.put("priority", priority);
            payload.put("requester", sender);
            payload.put("created_at", System.currentTimeMillis());

            String jsonPayload = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVICE_REQUEST_API))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "Cookie_Token=" + session.get("cookie_token"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 201) {
                            try {
                                Map<String, Object> result = mapper.readValue(response.body(), Map.class);
                                String srId = (String) result.get("service_request_id");
                                return Map.entry(srId, null);
                            } catch (Exception e) {
                                return Map.entry(null, "❌ Error parsing service request response");
                            }
                        } else {
                            return Map.entry(null, "❌ Failed to create service request: HTTP " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                Map.entry(null, "❌ Error creating service request: " + e.getMessage()));
        }
    }
}
