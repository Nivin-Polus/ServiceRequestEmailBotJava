package com.servicerequest.emailbot.service;

import com.servicerequest.emailbot.model.EmailData;
import com.servicerequest.emailbot.service.OutlookSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true", matchIfMissing = true)
public class EmailSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSchedulerService.class);

    @Value("${app.temp.directory:temp_attachments}")
    private String tempDirectory;

    @Autowired
    private OutlookAuthService outlookAuthService;

    @Autowired
    private OutlookSessionService outlookSessionService;

    @Autowired
    private EmailProcessorService emailProcessorService;

    @PostConstruct
    public void initialize() {
        logger.info("🚀 Starting Service Request Email Bot...");
        cleanupTempAttachments();
    }

    @Scheduled(fixedDelayString = "${app.email.check-interval:30}000")
    public void checkEmails() {
        logger.info("📧 [{}] Checking for unread emails...", LocalDateTime.now());
        
        try {
            String token = outlookAuthService.getAccessToken();
            OutlookSession outlookSession = outlookSessionService.createSession(token);
            
            List<EmailData> unreadEmails = outlookSession.getService()
                    .getUnreadEmails(outlookSession, outlookAuthService);

            for (EmailData email : unreadEmails) {
                String subject = email.subject().toLowerCase();
                if (subject.contains("service request")) {
                    emailProcessorService.processEmail(outlookSession, email, outlookAuthService)
                            .exceptionally(ex -> {
                                logger.error("❌ Error processing email: {}", ex.getMessage());
                                return null;
                            });
                } else {
                    logger.debug("Skipping email with subject: '{}'", subject);
                }
            }

        } catch (Exception e) {
            logger.error("🔥 Main loop error: {}", e.getMessage());
            try {
                String newToken = outlookAuthService.getAccessToken();
                outlookSessionService.refreshToken(newToken);
                logger.info("🔄 Refreshed Outlook token.");
            } catch (Exception reauthE) {
                logger.error("❌ Failed to refresh token: {}", reauthE.getMessage());
                try {
                    Thread.sleep(60_000); // sleep 60s if reauth fails
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void cleanupTempAttachments() {
        Path tempDir = Paths.get(System.getProperty("user.dir"), tempDirectory);
        if (Files.exists(tempDir)) {
            try {
                deleteDirectoryRecursively(tempDir);
                logger.info("🗑️ Cleaned up temp attachments directory");
            } catch (IOException e) {
                logger.warn("⚠️ Failed to cleanup temp directory: {}", e.getMessage());
            }
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
