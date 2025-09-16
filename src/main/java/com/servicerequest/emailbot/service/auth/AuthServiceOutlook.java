package com.servicerequest.emailbot.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@Primary
public class AuthServiceOutlook {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Dotenv dotenv;
    private String accessToken;
    private String refreshToken;
    private long tokenExpiryTime;

    public AuthServiceOutlook() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        // Don't initialize tokens in constructor to avoid startup failures
    }

    private void initializeTokens() throws Exception {
        String clientId = dotenv.get("OUTLOOK_CLIENT_ID");
        String tenantId = dotenv.get("OUTLOOK_TENANT_ID");

        if (clientId == null || tenantId == null) {
            throw new RuntimeException("OUTLOOK_CLIENT_ID and OUTLOOK_TENANT_ID environment variables must be set");
        }

        // Step 1: Initiate device code flow
        String deviceCodeUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/devicecode";
        
        Map<String, String> deviceCodeData = new HashMap<>();
        deviceCodeData.put("client_id", clientId);
        deviceCodeData.put("scope", "User.Read Mail.Read Mail.Send");

        String deviceCodeBody = deviceCodeData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");

        HttpRequest deviceCodeRequest = HttpRequest.newBuilder()
                .uri(URI.create(deviceCodeUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(deviceCodeBody))
                .build();

        HttpResponse<String> deviceCodeResponse = client.send(deviceCodeRequest, HttpResponse.BodyHandlers.ofString());
        
        if (deviceCodeResponse.statusCode() != 200) {
            System.err.println("Device code request failed with status: " + deviceCodeResponse.statusCode());
            System.err.println("Response body: " + deviceCodeResponse.body());
            throw new RuntimeException("Device code request failed: " + deviceCodeResponse.statusCode());
        }

        JsonNode deviceCodeJson = mapper.readTree(deviceCodeResponse.body());
        String deviceCode = deviceCodeJson.get("device_code").asText();
        String userCode = deviceCodeJson.get("user_code").asText();
        String verificationUri = deviceCodeJson.get("verification_uri").asText();
        int interval = deviceCodeJson.get("interval").asInt();
        int expiresIn = deviceCodeJson.get("expires_in").asInt();

        System.out.println("\n✅ OUTLOOK AUTHENTICATION REQUIRED ✅");
        System.out.println("Go to: " + verificationUri);
        System.out.println("Enter code: " + userCode);
        System.out.println("Waiting for authentication...\n");

        // Step 2: Poll for token
        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
        long startTime = System.currentTimeMillis();
        long timeoutMs = expiresIn * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Thread.sleep(interval * 1000L);

            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            tokenData.put("client_id", clientId);
            tokenData.put("device_code", deviceCode);

            String tokenBody = tokenData.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                    .build();

            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            
            if (tokenResponse.statusCode() == 200) {
                JsonNode tokenJson = mapper.readTree(tokenResponse.body());
                this.accessToken = tokenJson.get("access_token").asText();
                this.refreshToken = tokenJson.get("refresh_token") != null ? tokenJson.get("refresh_token").asText() : null;
                this.tokenExpiryTime = System.currentTimeMillis() + (tokenJson.get("expires_in").asLong() * 1000);
                System.out.println("✅ Authentication successful!");
                return;
            } else if (tokenResponse.statusCode() == 400) {
                JsonNode errorJson = mapper.readTree(tokenResponse.body());
                String error = errorJson.get("error").asText();
                
                if ("authorization_pending".equals(error)) {
                    continue; // Keep polling
                } else if ("authorization_declined".equals(error)) {
                    throw new RuntimeException("User declined the authentication request");
                } else if ("expired_token".equals(error)) {
                    throw new RuntimeException("Device code expired. Please restart the application.");
                } else {
                    throw new RuntimeException("Authentication error: " + error);
                }
            }
        }
        
        throw new RuntimeException("Authentication timed out");
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
