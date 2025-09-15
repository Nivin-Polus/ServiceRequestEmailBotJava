package com.servicerequest.emailbot.service.attachment;

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
    
    public boolean uploadAttachment(String srId, File attachment, String authToken) throws Exception {
        String apiUrl = System.getenv("ATTACHMENT_API");
        if (apiUrl == null) {
            throw new RuntimeException("ATTACHMENT_API environment variable not set");
        }

        // Implementation for attachment upload
        System.out.println("Uploading attachment: " + attachment.getName() + " for SR: " + srId);
        
        // TODO: Implement actual file upload logic
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
