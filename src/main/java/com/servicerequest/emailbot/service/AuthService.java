package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.JSONObject;

public class AuthService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String AUTH_API = dotenv.get("AUTH_API");

    /**
     * Logs into Service Tracker for a given emailId and returns session data.
     * Requires user credentials from UserCredentials.getCredentials(emailId).
     */
    public static Map<String, Object> getServiceTrackerSession(String emailId) {
        try {
            // Get stored credentials
            Map<String, String> creds = UserCredentials.getCredentials(emailId);
            if (creds == null || creds.isEmpty()) {
                throw new IllegalArgumentException("⚠️ No credentials found for email " + emailId);
            }

            // Create request body
            JSONObject payload = new JSONObject();
            payload.put("username", creds.get("AUTH_USERNAME"));
            payload.put("password", creds.get("AUTH_PASSWORD"));

            // HTTP client
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            // Send request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("❌ Service Tracker login failed: " + response.statusCode());
            }

            JSONObject trackerData = new JSONObject(response.body());

            // Extract Cookie_Token
            String cookieToken = null;
            Optional<String> setCookie = response.headers().firstValue("set-cookie");
            if (setCookie.isPresent()) {
                String cookieHeader = setCookie.get();
                if (cookieHeader.contains("Cookie_Token")) {
                    cookieToken = Arrays.stream(cookieHeader.split(";"))
                            .filter(s -> s.trim().startsWith("Cookie_Token"))
                            .map(s -> s.split("=")[1])
                            .findFirst()
                            .orElse(null);
                }
            }

            if (cookieToken == null || cookieToken.isEmpty()) {
                throw new RuntimeException("❌ No Cookie_Token found in Service Tracker login response");
            }

            // Build session data
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("email", emailId);

            // Add JSON response data into map
            for (String key : trackerData.keySet()) {
                sessionData.put(key, trackerData.get(key));
            }

            sessionData.put("cookie_token", cookieToken);

            System.out.println("✅ Service Tracker session created for " + emailId);
            return sessionData;

        } catch (Exception e) {
            System.out.println("❌ Service Tracker login error for " + emailId + ": " + e.getMessage());
            return null;
        }
    }
}
