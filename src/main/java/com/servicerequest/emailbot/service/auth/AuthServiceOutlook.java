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
public class AuthServiceOutlook {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private String accessToken;
    private String refreshToken;
    private long tokenExpiryTime;

    public AuthServiceOutlook() throws Exception {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        initializeTokens();
    }

    private void initializeTokens() throws Exception {
        String clientId = System.getenv("OUTLOOK_CLIENT_ID");
        String clientSecret = System.getenv("OUTLOOK_CLIENT_SECRET");
        String tenantId = System.getenv("OUTLOOK_TENANT_ID");

        if (clientId == null || clientSecret == null || tenantId == null) {
            throw new RuntimeException("Outlook authentication environment variables not set");
        }

        // OAuth2 client credentials flow for application permissions
        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
        
        Map<String, String> formData = new HashMap<>();
        formData.put("client_id", clientId);
        formData.put("client_secret", clientSecret);
        formData.put("scope", "https://graph.microsoft.com/.default");
        formData.put("grant_type", "client_credentials");

        String formBody = formData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = mapper.readTree(response.body());
            this.accessToken = responseJson.get("access_token").asText();
            int expiresIn = responseJson.get("expires_in").asInt();
            this.tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000);
            System.out.println("Outlook authentication successful");
        } else {
            throw new RuntimeException("Outlook authentication failed: " + response.statusCode());
        }
    }

    public String getValidAccessToken() throws Exception {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiryTime) {
            initializeTokens();
        }
        return accessToken;
    }

    public boolean isTokenValid() {
        return accessToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }
}
