package com.servicerequest.emailbot.service.auth;

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
public class AuthService {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Dotenv dotenv;
    private final Map<String, Map<String, String>> emailCredentials;
    private String authToken;
    private long tokenExpiryTime;

    public AuthService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(60))
                .build();
        this.mapper = new ObjectMapper();
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.emailCredentials = initializeEmailCredentials();
    }

    private Map<String, Map<String, String>> initializeEmailCredentials() {
        Map<String, Map<String, String>> credentials = new HashMap<>();

        credentials.put("nivin@polussolutions.com", Map.of(
                "AUTH_USERNAME", "willsmith",
                "AUTH_PASSWORD", "coi123"
        ));
        credentials.put("sanidh.id@polussolutions.com", Map.of(
                "AUTH_USERNAME", "Tanjiro",
                "AUTH_PASSWORD", "coi123"
        ));
        credentials.put("mahesh.sreenath@polussolutions.com", Map.of(
                "AUTH_USERNAME", "Tanjiro",
                "AUTH_PASSWORD", "coi123"
        ));
        credentials.put("mohammedijas.s@polussolutions.com", Map.of(
                "AUTH_USERNAME", "Tanjiro",
                "AUTH_PASSWORD", "coi123"
        ));
        credentials.put("anish.t@polussolutions.com", Map.of(
                "AUTH_USERNAME", "willsmith",
                "AUTH_PASSWORD", "coi123"
        ));
        credentials.put("robin.j@polussolutions.com", Map.of(
                "AUTH_USERNAME", "willsmith",
                "AUTH_PASSWORD", "coi123"
        ));
        credentials.put("rkprasad@polussolutions.com", Map.of(
                "AUTH_USERNAME", "willsmith",
                "AUTH_PASSWORD", "coi123"
        ));

        return credentials;
    }

    /**
     * ✅ Wrapper around getServiceTrackerSession
     * Returns only the token and caches it.
     */
    public String authenticate(String emailAddress) throws Exception {
        System.out.println("Starting authentication to service tracker for: " + emailAddress);

        Map<String, Object> session = getServiceTrackerSession(emailAddress);
        String token = (String) session.get("cookie_token");

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("No token found in service tracker session response");
        }

        this.authToken = token;
        this.tokenExpiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24h
        System.out.println("Token obtained and cached: SUCCESS");
        return this.authToken;
    }

    public Map<String, Object> getServiceTrackerSession(String emailId) throws Exception {
        Map<String, String> userCreds = emailCredentials.get(emailId.toLowerCase());
        if (userCreds == null) {
            throw new RuntimeException("No credentials found for email: " + emailId);
        }

        String authApi = dotenv.get("AUTH_API");
        if (authApi == null) {
            throw new RuntimeException("AUTH_API environment variable not set");
        }

        try {
            System.out.println("Getting service tracker session for: " + emailId);

            Map<String, Object> credentials = new HashMap<>();
            credentials.put("username", userCreds.get("AUTH_USERNAME"));
            credentials.put("password", userCreds.get("AUTH_PASSWORD"));

            String jsonBody = mapper.writeValueAsString(credentials);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authApi))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Session response status: " + response.statusCode());
            System.out.println("Session response body: " + response.body());

            if (response.statusCode() == 200) {
                JsonNode responseJson = mapper.readTree(response.body());

                // ✅ Aligning with Python: return same keys
                Map<String, Object> session = new HashMap<>();
                session.put("cookie_token", responseJson.path("token").asText("mock_cookie_" + userCreds.get("AUTH_USERNAME")));
                session.put("personID", responseJson.path("personID").asText("10000000001"));
                session.put("userName", responseJson.path("userName").asText(userCreds.get("AUTH_USERNAME")));
                session.put("fullName", responseJson.path("fullName").asText("System User"));
                session.put("unitNumber", responseJson.path("unitNumber").asText("000001"));
                session.put("orgUnit", responseJson.path("unitName").asText("IT Support")); // renamed key

                System.out.println("✅ Session data retrieved successfully");
                return session;
            } else {
                throw new RuntimeException("Session request failed: " + response.statusCode() + " - " + response.body());
            }
        } catch (java.net.ConnectException | java.net.http.HttpTimeoutException e) {
            System.err.println("❌ Service tracker not accessible: " + e.getMessage());
            throw new RuntimeException("Service tracker authentication failed - backend not accessible", e);
        }
    }

    public String getValidToken(String emailAddress) throws Exception {
        if (authToken == null || System.currentTimeMillis() > tokenExpiryTime) {
            return authenticate(emailAddress);
        }
        return authToken;
    }

    public boolean isTokenValid() {
        return authToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }
}
