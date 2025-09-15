package com.servicerequest.emailbot.service.auth;

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
public class AuthService {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private String authToken;
    private long tokenExpiryTime;

    public AuthService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String authenticate() throws Exception {
        String authApi = System.getenv("AUTH_API");
        if (authApi == null) {
            throw new RuntimeException("AUTH_API environment variable not set");
        }

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", System.getenv("AUTH_USERNAME"));
        credentials.put("password", System.getenv("AUTH_PASSWORD"));

        String jsonBody = mapper.writeValueAsString(credentials);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authApi))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = mapper.readTree(response.body());
            this.authToken = responseJson.get("token").asText();
            this.tokenExpiryTime = System.currentTimeMillis() + (3600 * 1000); // 1 hour
            System.out.println("Authentication successful");
            return this.authToken;
        } else {
            throw new RuntimeException("Authentication failed: " + response.statusCode());
        }
    }

    public String getValidToken() throws Exception {
        if (authToken == null || System.currentTimeMillis() > tokenExpiryTime) {
            return authenticate();
        }
        return authToken;
    }

    public boolean isTokenValid() {
        return authToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }
}
