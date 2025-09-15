package com.servicerequest.emailbot.service.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailSchedulerService {
    
    @Autowired
    private EmailProcessorService emailProcessorService;
    
    @Value("${app.email.check-interval:30000}")
    private long checkInterval;
    
    private boolean isProcessing = false;
    
    @Scheduled(fixedDelayString = "${app.email.check-interval:30000}")
    public void scheduleEmailCheck() {
        if (isProcessing) {
            System.out.println("Email processing already in progress, skipping this cycle");
            return;
        }
        
        try {
            isProcessing = true;
            System.out.println("Scheduled email check starting...");
            emailProcessorService.processEmails();
            System.out.println("Scheduled email check completed");
        } catch (Exception e) {
            System.err.println("Error in scheduled email check: " + e.getMessage());
        } finally {
            isProcessing = false;
        }
    }
    
    public void triggerManualCheck() {
        if (isProcessing) {
            System.out.println("Email processing already in progress");
            return;
        }
        
        try {
            isProcessing = true;
            System.out.println("Manual email check triggered");
            emailProcessorService.processEmails();
        } catch (Exception e) {
            System.err.println("Error in manual email check: " + e.getMessage());
        } finally {
            isProcessing = false;
        }
    }
    
    public boolean isCurrentlyProcessing() {
        return isProcessing;
    }
}
