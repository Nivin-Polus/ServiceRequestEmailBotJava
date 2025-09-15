package com.servicerequest.emailbot.service.core;

import com.servicerequest.emailbot.model.EmailData;
import com.servicerequest.emailbot.repository.DbService;
import com.servicerequest.emailbot.service.LocalStorageService;
import com.servicerequest.emailbot.service.ai.ClaudeService;
import com.servicerequest.emailbot.service.auth.AuthService;
import com.servicerequest.emailbot.service.auth.AuthServiceOutlook;
import com.servicerequest.emailbot.service.outlook.OutlookService;
import com.servicerequest.emailbot.service.outlook.OutlookSessionService;
import com.servicerequest.emailbot.service.servicerequest.ServiceRequest;
import com.servicerequest.emailbot.service.slack.AdaptiveCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailProcessorService {
    
    @Autowired
    private OutlookService outlookService;
    
    @Autowired
    private OutlookSessionService sessionService;
    
    @Autowired
    private AuthServiceOutlook outlookAuth;
    
    @Autowired
    private ClaudeService claudeService;
    
    @Autowired
    private ServiceRequest serviceRequestService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private LocalStorageService localStorageService;
    
    @Autowired
    private AdaptiveCardService slackService;
    
    @Autowired
    private DbService dbService;

    public void processEmails() {
        try {
            System.out.println("Starting email processing...");
            
            // Get unread emails
            List<EmailData> emails = outlookService.getUnreadEmails(
                sessionService.getCurrentSession(), 
                outlookAuth
            );
            
            System.out.println("Found " + emails.size() + " unread emails");
            
            for (EmailData email : emails) {
                processIndividualEmail(email);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing emails: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processIndividualEmail(EmailData email) {
        try {
            System.out.println("Processing email from: " + email.getSender() + " - " + email.getSubject());
            
            // Check if this is a thread we already know about
            Map<String, Object> existingMapping = localStorageService.getThreadMapping(email.getConversationId());
            
            if (existingMapping != null) {
                // This is a follow-up to an existing service request
                handleFollowUpEmail(email, (String) existingMapping.get("sr_id"));
            } else {
                // This is a new email that needs to become a service request
                handleNewEmail(email);
            }
            
            // Mark email as read
            outlookService.markAsRead(
                sessionService.getCurrentSession(), 
                outlookAuth, 
                email.getId()
            );
            
        } catch (Exception e) {
            System.err.println("Error processing individual email: " + e.getMessage());
        }
    }
    
    private void handleNewEmail(EmailData email) throws Exception {
        // Analyze email with Claude AI
        Map<String, String> analysis = claudeService.analyzeEmail(email.getBody(), email.getSubject());
        
        // Create service request
        String authToken = authService.getValidToken();
        String srId = serviceRequestService.createServiceRequest(
            Map.of(
                "subject", email.getSubject(),
                "description", email.getBody(),
                "category", analysis.get("category"),
                "type", analysis.get("type"),
                "priority", analysis.get("priority"),
                "requesterEmail", email.getSender()
            ),
            authToken
        );
        
        // Save thread mapping locally
        localStorageService.saveThreadMapping(
            email.getConversationId(), 
            srId, 
            email.getSender(), 
            email.getSubject()
        );
        
        // Send Slack notification
        slackService.sendSlackNotification(
            srId, 
            "New service request created from email: " + email.getSubject(), 
            null
        );
        
        System.out.println("Created new service request: " + srId);
    }
    
    private void handleFollowUpEmail(EmailData email, String srId) throws Exception {
        System.out.println("Processing follow-up email for SR: " + srId);
        
        // Add comment to existing service request
        // Implementation would go here based on your comment service
        
        // Send Slack notification for follow-up
        slackService.sendSlackNotification(
            srId, 
            "Follow-up received from " + email.getSender() + ": " + email.getSubject(), 
            null
        );
    }
}
