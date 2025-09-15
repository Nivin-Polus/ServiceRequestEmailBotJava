package com.servicerequest.emailbot.service.comments;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public CommentsService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public boolean addComment(String srId, String comment, String authToken) throws Exception {
        String apiUrl = System.getenv("COMMENTS_API");
        if (apiUrl == null) {
            throw new RuntimeException("COMMENTS_API environment variable not set");
        }

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
