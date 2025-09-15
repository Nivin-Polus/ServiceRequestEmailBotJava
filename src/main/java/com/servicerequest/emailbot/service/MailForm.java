package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
public class MailForm {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String SR_URL_BASE = dotenv.get("SERVICE_REQUEST_VIEW_URL",
            "http://localhost:2500/#/fibi/service-request/overview?serviceRequestId=");

    public static CompletableFuture<Map.Entry<String, String>> buildQuestionnaireEmail(
            String serviceRequestId,
            String emailId,
            Map<String, String> srDetails) {

        if (srDetails == null) {
            srDetails = new HashMap<>();
            srDetails.put("subject", "");
            srDetails.put("category", "");
            srDetails.put("type", "");
            srDetails.put("department", "");
            srDetails.put("priority", "");
        }

        return GetQuestionnaire.getQuestionnaire(serviceRequestId, emailId)
                .thenApply(result -> {
                    Map<String, Object> questionnaireData = result.getKey();
                    String error = result.getValue();

                    if (error != null) {
                        return Map.entry(null, error);
                    }

                    try {
                        String htmlContent = generateQuestionnaireHtml(serviceRequestId, emailId, 
                                srDetails, questionnaireData);
                        return Map.entry(htmlContent, null);
                    } catch (Exception e) {
                        return Map.entry(null, "❌ Error generating questionnaire HTML: " + e.getMessage());
                    }
                });
    }

    private static String generateQuestionnaireHtml(String serviceRequestId, String emailId,
            Map<String, String> srDetails, Map<String, Object> questionnaireData) {
        
        String srUrl = SR_URL_BASE + serviceRequestId;
        
        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Service Request Questionnaire</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { background-color: #f0f8ff; padding: 15px; border-radius: 5px; }
                    .question { margin: 15px 0; padding: 10px; border-left: 3px solid #007acc; }
                    .form-group { margin: 10px 0; }
                    input, textarea, select { width: 100%; padding: 8px; margin: 5px 0; }
                    .submit-btn { background-color: #007acc; color: white; padding: 10px 20px; border: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>📋 Service Request %s - Questionnaire</h2>
                    <p><strong>Category:</strong> %s</p>
                    <p><strong>Type:</strong> %s</p>
                    <p><strong>Priority:</strong> %s</p>
                    <p><a href="%s" target="_blank">View Service Request</a></p>
                </div>
            """.formatted(serviceRequestId, 
                         srDetails.get("category"), 
                         srDetails.get("type"), 
                         srDetails.get("priority"), 
                         srUrl));

        // Add questionnaire questions
        if (questionnaireData != null && questionnaireData.containsKey("questions")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) questionnaireData.get("questions");
            
            html.append("<form method='POST' action='/webhook/questionnaire'>");
            html.append("<input type='hidden' name='service_request_id' value='").append(serviceRequestId).append("'>");
            html.append("<input type='hidden' name='email_id' value='").append(emailId).append("'>");
            html.append("<input type='hidden' name='action' value='submit_questionnaire'>");
            
            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> question = questions.get(i);
                String questionText = (String) question.get("text");
                String questionType = (String) question.getOrDefault("type", "text");
                
                html.append("<div class='question'>");
                html.append("<div class='form-group'>");
                html.append("<label><strong>Q").append(i + 1).append(":</strong> ").append(questionText).append("</label>");
                
                if ("textarea".equals(questionType)) {
                    html.append("<textarea name='q").append(i + 1).append("' rows='3'></textarea>");
                } else {
                    html.append("<input type='text' name='q").append(i + 1).append("'>");
                }
                
                html.append("</div>");
                html.append("</div>");
            }
            
            html.append("<div class='form-group'>");
            html.append("<label><strong>Additional Comments:</strong></label>");
            html.append("<textarea name='comments' rows='4' placeholder='Any additional information...'></textarea>");
            html.append("</div>");
            
            html.append("<button type='submit' class='submit-btn'>Submit Questionnaire</button>");
            html.append("</form>");
        }

        html.append("""
                <div style="margin-top: 20px; font-size: 12px; color: #666;">
                    <p>This is an automated email from the Service Request System.</p>
                </div>
            </body>
            </html>
            """);

        return html.toString();
    }
}
