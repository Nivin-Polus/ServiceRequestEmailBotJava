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
public class ServiceRequestId {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ServiceRequestId() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String generateServiceRequestId(String category, String type) throws Exception {
        String apiUrl = System.getenv("SR_ID_GENERATOR_API");
        if (apiUrl == null) {
            // Generate a simple ID if no API is available
            return generateSimpleId(category, type);
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("category", category);
        requestData.put("type", type);
        requestData.put("timestamp", System.currentTimeMillis());

        String jsonBody = mapper.writeValueAsString(requestData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = mapper.readTree(response.body());
            return responseJson.get("serviceRequestId").asText();
        } else {
            // Fallback to simple ID generation
            return generateSimpleId(category, type);
        }
    }

    private String generateSimpleId(String category, String type) {
        String categoryPrefix = category != null && category.length() >= 2 ? 
            category.substring(0, 2).toUpperCase() : "SR";
        String typePrefix = type != null && type.length() >= 2 ? 
            type.substring(0, 2).toUpperCase() : "GN";
        
        long timestamp = System.currentTimeMillis();
        String timestampSuffix = String.valueOf(timestamp).substring(8); // Last 5 digits
        
        return categoryPrefix + typePrefix + timestampSuffix;
    }

    public boolean isValidServiceRequestId(String srId) {
        if (srId == null || srId.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation - should be alphanumeric and reasonable length
        return srId.matches("^[A-Z0-9]{6,20}$");
    }
}
