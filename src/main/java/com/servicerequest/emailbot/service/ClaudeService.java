package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class ClaudeService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLAUDE_API_KEY = dotenv.get("CLAUDE_API_KEY");
    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";

    // Remove static initialization that causes circular dependency
    private Map<String, Object> categoryMap;

    static {
        if (CLAUDE_API_KEY == null || CLAUDE_API_KEY.isBlank()) {
            throw new IllegalArgumentException("❌ CLAUDE_API_KEY is missing in your .env file!");
        }
    }

    public Map<String, Object> classifyServiceRequest(String emailBody, String subject, String sender) {
        try {
            String prompt = buildClassificationPrompt(emailBody, subject, sender);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "claude-3-sonnet-20240229");
            requestBody.put("max_tokens", 1000);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            requestBody.put("messages", messages);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CLAUDE_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", CLAUDE_API_KEY)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Claude API error: " + response.statusCode());
            }

            JSONObject responseJson = new JSONObject(response.body());
            String content = responseJson.getJSONArray("content")
                    .getJSONObject(0)
                    .getString("text");

            return parseClassificationResponse(content);

        } catch (Exception e) {
            System.err.println("❌ Claude classification error: " + e.getMessage());
            return getDefaultClassification();
        }
    }

    private String buildClassificationPrompt(String emailBody, String subject, String sender) {
        return """
            Classify this service request email into appropriate categories.
            
            Subject: %s
            From: %s
            Body: %s
            
            Please respond with JSON format:
            {
                "category": "IT Support|HR|Facilities|Finance",
                "type": "Hardware|Software|Access|General",
                "department": "IT|HR|Facilities|Finance",
                "priority": "Low|Medium|High|Critical",
                "description": "Brief description of the request"
            }
            """.formatted(subject, sender, emailBody);
    }

    private Map<String, Object> parseClassificationResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            Map<String, Object> result = new HashMap<>();
            result.put("category", json.optString("category", "General"));
            result.put("type", json.optString("type", "General"));
            result.put("department", json.optString("department", "IT"));
            result.put("priority", json.optString("priority", "Medium"));
            result.put("description", json.optString("description", "Service request"));
            return result;
        } catch (Exception e) {
            return getDefaultClassification();
        }
    }

    private Map<String, Object> getDefaultClassification() {
        Map<String, Object> result = new HashMap<>();
        result.put("category", "General");
        result.put("type", "General");
        result.put("department", "IT");
        result.put("priority", "Medium");
        result.put("description", "Service request");
        return result;
    }
}
