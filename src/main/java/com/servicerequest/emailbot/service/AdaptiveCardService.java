package com.servicerequest.emailbot.service;

import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class AdaptiveCardService {
    
    public CompletableFuture<String> buildQuestionnaireEmail(String srId, String sender, ServiceRequest.ClassificationResult classification) {
        return CompletableFuture.supplyAsync(() -> {
            // Mock questionnaire HTML
            return """
                <html>
                <body>
                    <h2>📋 Service Request %s - Questionnaire</h2>
                    <p>Dear %s,</p>
                    <p>Thank you for submitting your service request. Please provide additional details:</p>
                    <form>
                        <p><strong>Q1:</strong> What is the urgency of this request?</p>
                        <input type="text" name="q1" placeholder="Your answer">
                        
                        <p><strong>Q2:</strong> When did this issue first occur?</p>
                        <input type="text" name="q2" placeholder="Your answer">
                        
                        <p><strong>Comments:</strong></p>
                        <textarea name="comments" placeholder="Additional comments"></textarea>
                        
                        <button type="submit">Submit Response</button>
                    </form>
                </body>
                </html>
                """.formatted(srId, sender);
        });
    }
}
