package com.servicerequest.emailbot.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
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
    private final Dotenv dotenv;
    private final String apiKey;

    public ClaudeService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.apiKey = dotenv.get("CLAUDE_API_KEY");
    }

    public Map<String, String> analyzeEmail(String emailContent, String subject) throws Exception {
        if (apiKey == null) {
            throw new RuntimeException("CLAUDE_API_KEY environment variable not set");
        }

        System.out.println("Building Claude prompt...");
        String prompt = buildAnalysisPrompt(emailContent, subject);
        System.out.println("Sending request to Claude API...");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-haiku-20240307");
        requestBody.put("max_tokens", 400);
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", new Object[]{
                Map.of("type", "text", "text", prompt)
            })
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
        System.out.println("Claude API response status: " + response.statusCode());
        
        if (response.statusCode() == 200) {
            System.out.println("Parsing Claude response...");
            JsonNode responseJson = mapper.readTree(response.body());
            String analysisResult = responseJson.get("content").get(0).get("text").asText();
            System.out.println("Claude raw response: " + analysisResult);
            return parseAnalysisResult(analysisResult);
        } else {
            System.err.println("Claude API error: " + response.statusCode() + " - " + response.body());
            throw new RuntimeException("Claude API error: " + response.statusCode() + " - " + response.body());
        }
    }

    private String buildAnalysisPrompt(String emailContent, String subject) {
        return "You are a classification assistant.\n" +
               "Here are the categories and types available in the system:\n" +
               "{\n" +
               "  \"IT Support\": [\"Hardware Issue\", \"Software Issue\", \"Network Issue\", \"Access Request\"],\n" +
               "  \"HR\": [\"Leave Request\", \"Policy Question\", \"Benefits\", \"Training\"],\n" +
               "  \"Finance\": [\"Expense Report\", \"Budget Request\", \"Invoice Query\", \"Payment Issue\"],\n" +
               "  \"Facilities\": [\"Maintenance Request\", \"Room Booking\", \"Equipment Request\", \"Security Issue\"]\n" +
               "}\n\n" +
               "For the following user input, generate a JSON with these exact keys:\n" +
               "- category\n" +
               "- type\n" +
               "- department\n" +
               "- priority (Low, Medium, or High)\n" +
               "- subject (concise, max 10 words)\n" +
               "- description (1-2 clear sentences)\n\n" +
               "⚠️ Important:\n" +
               "- category and type MUST be chosen from the provided list above.\n" +
               "- department must be relevant to the request (if not sure, default to \"IT Support\").\n" +
               "- priority: Low = minor/non-urgent, Medium = normal, High = urgent/critical.\n\n" +
               "Email Subject: " + subject + "\n" +
               "Email Content: " + emailContent;
    }

    private Map<String, String> parseAnalysisResult(String analysisResult) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode json = mapper.readTree(analysisResult);
            result.put("category", json.get("category").asText());
            result.put("type", json.get("type").asText());
            result.put("department", json.get("department").asText());
            result.put("priority", json.get("priority").asText());
            result.put("subject", json.get("subject").asText());
            result.put("description", json.get("description").asText());
        } catch (Exception e) {
            // Fallback parsing if JSON parsing fails
            result.put("category", "IT Support");
            result.put("type", "Software Issue");
            result.put("department", "IT Support");
            result.put("priority", "Medium");
            result.put("subject", "Email processing request");
            result.put("description", "Email analysis failed - manual review required");
        }
        return result;
    }
}
