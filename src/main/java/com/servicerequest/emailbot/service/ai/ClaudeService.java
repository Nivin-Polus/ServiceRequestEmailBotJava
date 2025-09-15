package com.servicerequest.emailbot.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClaudeService {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String apiKey;

    public ClaudeService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.apiKey = System.getenv("CLAUDE_API_KEY");
    }

    public Map<String, String> analyzeEmail(String emailContent, String subject) throws Exception {
        if (apiKey == null) {
            throw new RuntimeException("CLAUDE_API_KEY environment variable not set");
        }

        String prompt = buildAnalysisPrompt(emailContent, subject);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-sonnet-20240229");
        requestBody.put("max_tokens", 1000);
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = mapper.readTree(response.body());
            String analysisResult = responseJson.get("content").get(0).get("text").asText();
            return parseAnalysisResult(analysisResult);
        } else {
            throw new RuntimeException("Claude API error: " + response.statusCode() + " - " + response.body());
        }
    }

    private String buildAnalysisPrompt(String emailContent, String subject) {
        return "Analyze this email and categorize it for service request creation. " +
               "Email Subject: " + subject + "\n" +
               "Email Content: " + emailContent + "\n\n" +
               "Please provide the following information in JSON format:\n" +
               "{\n" +
               "  \"category\": \"category_name\",\n" +
               "  \"type\": \"type_name\",\n" +
               "  \"priority\": \"high|medium|low\",\n" +
               "  \"summary\": \"brief_summary\",\n" +
               "  \"confidence\": \"0.0-1.0\"\n" +
               "}";
    }

    private Map<String, String> parseAnalysisResult(String analysisResult) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode json = mapper.readTree(analysisResult);
            result.put("category", json.get("category").asText());
            result.put("type", json.get("type").asText());
            result.put("priority", json.get("priority").asText());
            result.put("summary", json.get("summary").asText());
            result.put("confidence", json.get("confidence").asText());
        } catch (Exception e) {
            // Fallback parsing if JSON parsing fails
            result.put("category", "OTHER");
            result.put("type", "GENERAL");
            result.put("priority", "medium");
            result.put("summary", "Email analysis failed");
            result.put("confidence", "0.1");
        }
        return result;
    }
}
