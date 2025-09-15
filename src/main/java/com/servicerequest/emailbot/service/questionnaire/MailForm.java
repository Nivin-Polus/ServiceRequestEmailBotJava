package com.servicerequest.emailbot.service.questionnaire;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MailForm {
    
    public String buildQuestionnaireForm(String questionnaire, String srId) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Service Request Questionnaire</h2>");
        html.append("<p>Service Request ID: ").append(srId).append("</p>");
        html.append("<form>");
        
        // Parse questionnaire and build form fields
        String[] questions = questionnaire.split("\n");
        int questionNumber = 1;
        
        for (String question : questions) {
            if (question.trim().isEmpty()) continue;
            
            html.append("<div style='margin-bottom: 15px;'>");
            html.append("<label><strong>").append(questionNumber).append(". ").append(question.trim()).append("</strong></label><br>");
            html.append("<textarea name='question_").append(questionNumber).append("' rows='3' cols='50' style='width: 100%; margin-top: 5px;'></textarea>");
            html.append("</div>");
            
            questionNumber++;
        }
        
        html.append("<div style='margin-top: 20px;'>");
        html.append("<p><strong>Please reply to this email with your answers.</strong></p>");
        html.append("</div>");
        html.append("</form>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    public Map<String, String> parseQuestionnaireResponse(String emailContent) {
        Map<String, String> answers = new HashMap<>();
        
        // Pattern to match numbered questions and answers
        Pattern pattern = Pattern.compile("(\\d+)\\.(.*?)(?=\\d+\\.|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(emailContent);
        
        while (matcher.find()) {
            String questionNumber = matcher.group(1);
            String answer = matcher.group(2).trim();
            
            if (!answer.isEmpty()) {
                answers.put("question_" + questionNumber, answer);
            }
        }
        
        return answers;
    }
    
    public boolean isQuestionnaireResponse(String emailContent, String subject) {
        // Check if email contains questionnaire response patterns
        return subject.toLowerCase().contains("questionnaire") ||
               subject.toLowerCase().contains("service request") ||
               emailContent.matches(".*\\d+\\..*") ||
               emailContent.toLowerCase().contains("answer") ||
               emailContent.toLowerCase().contains("response");
    }
    
    public String extractServiceRequestId(String emailContent, String subject) {
        // Pattern to match SR ID in various formats
        Pattern srPattern = Pattern.compile("(?:SR|Service Request|ID)[:\\s#]*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE);
        
        // First try subject line
        Matcher matcher = srPattern.matcher(subject);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Then try email content
        matcher = srPattern.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
}
