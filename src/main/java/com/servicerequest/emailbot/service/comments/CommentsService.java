package com.servicerequest.emailbot.service.comments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicerequest.emailbot.service.auth.AuthService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommentsService {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Dotenv dotenv;
    
    @Autowired
    private AuthService authService;

    public CommentsService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    public boolean addComment(String srId, String comment, String senderEmail) throws Exception {
        String apiUrl = dotenv.get("COMMENTS_API");
        if (apiUrl == null) {
            throw new RuntimeException("COMMENTS_API environment variable not set");
        }

        // Get authentication token for the sender
        String authToken = authService.getValidToken(senderEmail);
        System.out.println("Adding comment to SR: " + srId + " (sender: " + senderEmail + ")");

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("serviceRequestId", srId);
        commentData.put("comment", comment);
        commentData.put("timestamp", System.currentTimeMillis());

        String jsonBody = mapper.writeValueAsString(commentData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Comment added successfully to SR: " + srId);
            return true;
        } else {
            System.err.println("Failed to add comment: " + response.statusCode() + " - " + response.body());
            return false;
        }
    }
}
