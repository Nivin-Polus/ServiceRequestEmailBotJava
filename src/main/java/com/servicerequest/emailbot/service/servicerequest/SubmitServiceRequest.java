package com.servicerequest.emailbot.service.servicerequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubmitServiceRequest {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public SubmitServiceRequest() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public boolean submitServiceRequest(String srId, String authToken) throws Exception {
        String apiUrl = System.getenv("SUBMIT_SR_API");
        if (apiUrl == null) {
            throw new RuntimeException("SUBMIT_SR_API environment variable not set");
        }

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("serviceRequestId", srId);
        submitData.put("action", "submit");
        submitData.put("submittedAt", System.currentTimeMillis());

        String jsonBody = mapper.writeValueAsString(submitData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Service request submitted successfully: " + srId);
            return true;
        } else {
            System.err.println("Failed to submit service request: " + response.statusCode() + " - " + response.body());
            return false;
        }
    }

    public boolean withdrawServiceRequest(String srId, String reason, String authToken) throws Exception {
        String apiUrl = System.getenv("SUBMIT_SR_API");
        if (apiUrl == null) {
            throw new RuntimeException("SUBMIT_SR_API environment variable not set");
        }

        Map<String, Object> withdrawData = new HashMap<>();
        withdrawData.put("serviceRequestId", srId);
        withdrawData.put("action", "withdraw");
        withdrawData.put("reason", reason);
        withdrawData.put("withdrawnAt", System.currentTimeMillis());

        String jsonBody = mapper.writeValueAsString(withdrawData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    }
}
