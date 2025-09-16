package com.servicerequest.emailbot.service.attachment;

import com.servicerequest.emailbot.service.auth.AuthService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class AttachmentService {
    
    @Autowired
    private AuthService authService;
    
    private final Dotenv dotenv;
    
    public AttachmentService() {
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }
    
    public boolean uploadAttachment(String srId, File attachment, String senderEmail) throws Exception {
        String apiUrl = dotenv.get("ATTACHMENT_API");
        if (apiUrl == null) {
            throw new RuntimeException("ATTACHMENT_API environment variable not set");
        }

        // Get authentication token for the sender
        String authToken = authService.getValidToken(senderEmail);
        System.out.println("Uploading attachment: " + attachment.getName() + " for SR: " + srId + " (sender: " + senderEmail + ")");
        
        // TODO: Implement actual file upload logic with authToken
        return true;
    }
    
    public File downloadAttachment(String attachmentId, String downloadPath) throws IOException {
        // Implementation for attachment download
        Path path = Paths.get(downloadPath, "attachment_" + attachmentId);
        File file = path.toFile();
        
        // Create file if it doesn't exist
        if (!file.exists()) {
            file.createNewFile();
        }
        
        return file;
    }
    
    public boolean validateAttachment(File attachment) {
        if (attachment == null || !attachment.exists()) {
            return false;
        }
        
        // Check file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (attachment.length() > maxSize) {
            return false;
        }
        
        // Check file extension
        String fileName = attachment.getName().toLowerCase();
        List<String> allowedExtensions = List.of(".pdf", ".doc", ".docx", ".txt", ".jpg", ".jpeg", ".png", ".gif");
        
        return allowedExtensions.stream().anyMatch(fileName::endsWith);
    }
}
