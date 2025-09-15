package com.servicerequest.emailbot.service.servicerequest;

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
public class ServiceRequest {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ServiceRequest() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String createServiceRequest(Map<String, Object> requestData, String authToken) throws Exception {
        String apiUrl = System.getenv("SERVICE_REQUEST_API");
        if (apiUrl == null) {
            throw new RuntimeException("SERVICE_REQUEST_API environment variable not set");
        }

        String jsonBody = mapper.writeValueAsString(requestData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            JsonNode responseJson = mapper.readTree(response.body());
            String srId = responseJson.get("serviceRequestId").asText();
            System.out.println("Service request created successfully: " + srId);
            return srId;
        } else {
            throw new RuntimeException("Failed to create service request: " + response.statusCode() + " - " + response.body());
        }
    }

    public boolean updateServiceRequest(String srId, Map<String, Object> updateData, String authToken) throws Exception {
        String apiUrl = System.getenv("SERVICE_REQUEST_API");
        if (apiUrl == null) {
            throw new RuntimeException("SERVICE_REQUEST_API environment variable not set");
        }

        updateData.put("serviceRequestId", srId);
        String jsonBody = mapper.writeValueAsString(updateData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .method("PUT", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    }
}
