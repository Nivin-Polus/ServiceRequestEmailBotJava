package com.servicerequest.emailbot.service.outlook;

import com.servicerequest.emailbot.model.EmailData;
import com.servicerequest.emailbot.service.AuthServiceOutlook;
import com.servicerequest.emailbot.service.OutlookSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Component
public class OutlookService {
    private static final String GRAPH_API_BASE = "https://graph.microsoft.com/v1.0";
    
    private final HttpClient client;
    private final ObjectMapper mapper;

    public OutlookService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public List<EmailData> getUnreadEmails(OutlookSession session, AuthServiceOutlook auth) throws Exception {
        String url = GRAPH_API_BASE + "/me/messages?$filter=isRead eq false&$top=50";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + session.getAuthToken())
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get unread emails: " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode emails = root.get("value");
        
        List<EmailData> emailList = new ArrayList<>();
        for (JsonNode email : emails) {
            EmailData emailData = new EmailData(
                email.get("id").asText(),
                email.get("from").get("emailAddress").get("address").asText(),
                email.get("subject").asText(),
                email.get("body").get("content").asText(),
                email.get("conversationId").asText(),
                email.get("hasAttachments").asBoolean()
            );
            emailList.add(emailData);
        }
        
        return emailList;
    }

    public void markAsRead(OutlookSession session, AuthServiceOutlook auth, String messageId) throws Exception {
        String url = GRAPH_API_BASE + "/me/messages/" + messageId;
        String body = "{\"isRead\": true}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + session.getAuthToken())
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to mark email as read: " + response.statusCode());
        }
    }

    public void sendEmail(OutlookSession session, AuthServiceOutlook auth, String to, String subject, String body, String replyToMessageId) throws Exception {
        String url = GRAPH_API_BASE + "/me/sendMail";
        
        Map<String, Object> emailBody = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        
        message.put("subject", subject);
        Map<String, Object> bodyContent = new HashMap<>();
        bodyContent.put("contentType", "HTML");
        bodyContent.put("content", body);
        message.put("body", bodyContent);
        
        List<Map<String, Object>> toRecipients = new ArrayList<>();
        Map<String, Object> recipient = new HashMap<>();
        Map<String, Object> emailAddress = new HashMap<>();
        emailAddress.put("address", to);
        recipient.put("emailAddress", emailAddress);
        toRecipients.add(recipient);
        message.put("toRecipients", toRecipients);
        
        emailBody.put("message", message);
        
        String jsonBody = mapper.writeValueAsString(emailBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + session.getAuthToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 202) {
            throw new RuntimeException("Failed to send email: " + response.statusCode());
        }
    }

    public void sendQuestionnaireForm(OutlookSession session, AuthServiceOutlook auth, String to, String subject, String htmlContent, String replyToMessageId) throws Exception {
        sendEmail(session, auth, to, subject, htmlContent, replyToMessageId);
    }
}
