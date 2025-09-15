package com.servicerequest.emailbot.service.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdaptiveCardService {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public AdaptiveCardService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public boolean sendSlackNotification(String srId, String message, String channel) throws Exception {
        String slackToken = System.getenv("SLACK_BOT_TOKEN");
        if (slackToken == null) {
            System.out.println("SLACK_BOT_TOKEN not set, skipping Slack notification");
            return false;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channel != null ? channel : "#service-requests");
        payload.put("text", "Service Request Update");
        payload.put("attachments", new Object[]{
            Map.of(
                "color", "good",
                "title", "Service Request: " + srId,
                "text", message,
                "footer", "Email Bot",
                "ts", System.currentTimeMillis() / 1000
            )
        });

        String jsonBody = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://slack.com/api/chat.postMessage"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + slackToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            System.out.println("Slack notification sent for SR: " + srId);
            return true;
        } else {
            System.err.println("Failed to send Slack notification: " + response.statusCode());
            return false;
        }
    }

    public Map<String, Object> createAdaptiveCard(String title, String message, String srId) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.3");
        
        Map<String, Object> titleBlock = new HashMap<>();
        titleBlock.put("type", "TextBlock");
        titleBlock.put("text", title);
        titleBlock.put("weight", "Bolder");
        titleBlock.put("size", "Medium");
        
        Map<String, Object> messageBlock = new HashMap<>();
        messageBlock.put("type", "TextBlock");
        messageBlock.put("text", message);
        messageBlock.put("wrap", true);
        
        Map<String, Object> srBlock = new HashMap<>();
        srBlock.put("type", "TextBlock");
        srBlock.put("text", "Service Request ID: " + srId);
        srBlock.put("weight", "Lighter");
        srBlock.put("size", "Small");
        
        card.put("body", new Object[]{titleBlock, messageBlock, srBlock});
        
        return card;
    }
}
