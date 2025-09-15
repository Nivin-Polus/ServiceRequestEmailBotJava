package com.servicerequest.emailbot.service.questionnaire;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GetQuestionnaire {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public GetQuestionnaire() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String getQuestionnaireForCategory(String category, String type) throws Exception {
        // Implementation for getting questionnaire based on category and type
        String apiUrl = System.getenv("QUESTIONNAIRE_API");
        if (apiUrl == null) {
            throw new RuntimeException("QUESTIONNAIRE_API environment variable not set");
        }

        String url = apiUrl + "?category=" + category + "&type=" + type;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode root = mapper.readTree(response.body());
            return root.get("questionnaire").asText();
        } else {
            throw new RuntimeException("Failed to get questionnaire: " + response.statusCode());
        }
    }

    public boolean hasQuestionnaire(String category, String type) {
        try {
            String questionnaire = getQuestionnaireForCategory(category, type);
            return questionnaire != null && !questionnaire.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking questionnaire availability: " + e.getMessage());
            return false;
        }
    }
}
