package com.servicerequest.emailbot.service;

import com.servicerequest.emailbot.model.EmailData;
import com.servicerequest.emailbot.util.EmailUtils;
import com.servicerequest.emailbot.service.OutlookSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(EmailProcessorService.class);

    @Autowired
    private OutlookServiceImpl outlookService;

    @Autowired
    private SRServiceImpl srService;

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private AttachmentServiceImpl attachmentService;

    @Autowired
    private AdaptiveCardServiceImpl adaptiveCardService;

    public CompletableFuture<Void> processEmail(OutlookSession session, EmailData email, OutlookAuthService auth) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("📧 Processing email: {}", email.subject());
                
                String conversationId = email.conversationId();
                String sender = email.sender();
                String subject = email.subject();
                String body = email.body();
                String messageId = email.messageId();
                boolean hasAttachments = email.hasAttachments();

                // Check if this is a follow-up to existing SR
                if (conversationId != null && srService.isExistingThread(conversationId)) {
                    handleExistingThread(session, auth, email, conversationId);
                    return;
                }

                // Process new service request
                processNewServiceRequest(session, auth, email);
                
            } catch (Exception e) {
                logger.error("❌ Error processing email {}: {}", email.messageId(), e.getMessage(), e);
            }
        });
    }

    private void handleExistingThread(OutlookSession session, OutlookAuthService auth, 
                                    EmailData email, String conversationId) {
        try {
            // Get existing SR data
            var existingSrData = srService.getSRByThread(conversationId);
            String existingSrId = existingSrData != null ? (String) existingSrData.get("sr_id") : null;
            
            if (existingSrId == null) {
                logger.error("❌ Invalid SR data in thread mapping: {}", existingSrData);
                return;
            }

            logger.info("📧 This email thread already has Service Request: {}", existingSrId);

            // Check if it's a questionnaire response
            if (EmailUtils.isQuestionnaireResponse(email.body(), email.subject())) {
                logger.info("📝 Processing questionnaire response for SR {}", existingSrId);
                // Handle questionnaire response logic here
            } else {
                EmailUtils.handleFollowupComment(session, auth, email.messageId(), 
                    email.sender(), email.subject(), email.body(), 
                    email.hasAttachments(), existingSrId);
            }
        } catch (Exception e) {
            logger.error("❌ Error handling existing thread: {}", e.getMessage(), e);
        }
    }

    private void processNewServiceRequest(OutlookSession session, OutlookAuthService auth, EmailData email) {
        try {
            logger.info("🆕 Processing new service request from: {}", email.sender());
            
            // Download attachments if any
            if (email.hasAttachments()) {
                logger.info("📎 Email has attachments - processing...");
                // Attachment processing logic would go here
            }

            // For now, just mark as read and send confirmation
            outlookService.markAsRead(session, auth, email.messageId());
            logger.info("✅ Marked email as read: {}", email.messageId());
            
        } catch (Exception e) {
            logger.error("❌ Error processing new service request: {}", e.getMessage(), e);
        }
    }
}
