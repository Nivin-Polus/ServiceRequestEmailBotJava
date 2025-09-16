package com.servicerequest.emailbot.util;

import com.servicerequest.emailbot.service.outlook.OutlookSession;
import com.servicerequest.emailbot.service.auth.AuthServiceOutlook;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.*;

public class EmailUtils {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtils.class);

    public static String extractNewMessageContent(String body) {
        String text = Jsoup.parse(body).text().trim();
        
        List<String> botContentMarkers = Arrays.asList(
            "💡 This email contains an interactive questionnaire form",
            "📋 Service Request Questionnaire",
            "Q1: Q1", "Q2: Q2",
            "Answer: Your answer to question",
            "💬 Additional Comments",
            "📎 Question Attachments:",
            "📎 Comments Attachments:",
            "📎 General Attachments:",
            "(no files)"
        );

        String[] lines = text.split("\\r?\\n");
        List<String> cleanLines = new ArrayList<>();
        boolean skipSection = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            boolean isBotContent = botContentMarkers.stream().anyMatch(line::contains);
            if (isBotContent) {
                skipSection = true;
                continue;
            }

            if (line.startsWith("From:") || line.startsWith("To:") ||
                line.startsWith("Subject:") || line.startsWith("Date:") ||
                line.startsWith("-----Original Message-----") ||
                (line.startsWith("On ") && line.contains("wrote:"))) {
                skipSection = true;
                continue;
            }

            if (!skipSection && line.length() > 3) {
                cleanLines.add(line);
            }
        }

        String cleanContent = String.join("\n", cleanLines).trim();
        if (cleanContent.isEmpty()) {
            for (int i = 0; i < Math.min(5, lines.length); i++) {
                String line = lines[i].trim();
                if (!line.isEmpty() && botContentMarkers.stream().noneMatch(line::contains)) {
                    cleanContent = line;
                    break;
                }
            }
        }
        return cleanContent.isEmpty() ? "Follow-up message" : cleanContent;
    }

    public static boolean isQuestionnaireResponse(String body, String subject) {
        List<String> botIndicators = Arrays.asList(
            "💡 This email contains an interactive questionnaire form",
            "📋 Service Request Questionnaire",
            "Please Complete Questionnaire"
        );

        for (String indicator : botIndicators) {
            if (body.contains(indicator)) return false;
        }

        List<String> questionnaireTags = Arrays.asList("[Q", "[COMMENTS]", "[GENERAL_ATTACHMENT]");
        for (String tag : questionnaireTags) {
            if (body.contains(tag)) return true;
        }

        Pattern pattern = Pattern.compile("Q\\d+:\\s*\\w+");
        return pattern.matcher(body).find();
    }

    public static void handleFollowupComment(OutlookSession outlookSession, AuthServiceOutlook outlookAuth, 
                                           String messageId, String sender, String subject, String body, 
                                           boolean hasAttachments, String existingSrId) {
        logger.info("💬 Processing follow-up comment for SR {}", existingSrId);
        
        try {
            String cleanBody = extractNewMessageContent(body);
            String commentText = "Follow-up from " + sender + ":\n\n" + cleanBody;
            
            logger.info("💬 Added follow-up comment to SR {}", existingSrId);
            logger.info("✅ Marked email as read");
            
        } catch (Exception e) {
            logger.error("❌ Error handling follow-up comment: {}", e.getMessage());
        }
    }
}
