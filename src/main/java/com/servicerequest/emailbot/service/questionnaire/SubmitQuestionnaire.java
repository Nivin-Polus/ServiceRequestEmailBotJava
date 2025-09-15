package com.servicerequest.emailbot.service.questionnaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubmitQuestionnaire {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public SubmitQuestionnaire() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public boolean submitQuestionnaireResponse(String srId, Map<String, String> answers, String authToken) throws Exception {
        String apiUrl = System.getenv("SUBMIT_QUESTIONNAIRE_API");
        if (apiUrl == null) {
            throw new RuntimeException("SUBMIT_QUESTIONNAIRE_API environment variable not set");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("serviceRequestId", srId);
        payload.put("answers", answers);
        payload.put("submittedAt", System.currentTimeMillis());

        String jsonBody = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Questionnaire response submitted successfully for SR: " + srId);
            return true;
        } else {
            System.err.println("Failed to submit questionnaire response: " + response.statusCode() + " - " + response.body());
            return false;
        }
    }

    public boolean validateAnswers(Map<String, String> answers) {
        if (answers == null || answers.isEmpty()) {
            return false;
        }

        // Check if at least one answer is provided
        for (String answer : answers.values()) {
            if (answer != null && !answer.trim().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}
