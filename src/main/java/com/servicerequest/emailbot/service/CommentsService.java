package com.servicerequest.emailbot.service;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CommentsService {
    
    public void postComment(String srId, String sender, String commentText, List<String> attachments) {
        // Mock implementation - would post to service tracker
        System.out.println("Posted comment to SR " + srId + " from " + sender);
        System.out.println("Comment: " + commentText);
    }
}
