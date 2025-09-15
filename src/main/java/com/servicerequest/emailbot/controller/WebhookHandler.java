package com.servicerequest.emailbot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/webhook")
public class WebhookHandler {

    // POST: /webhook/questionnaire
    @PostMapping(value = "/questionnaire", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleQuestionnaireSubmission(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !"submit_questionnaire".equals(payload.get("action"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid request"));
            }

            String serviceRequestId = (String) payload.get("service_request_id");
            String emailId = (String) payload.get("email_id");
            Map<String, Object> responses = (Map<String, Object>) payload.getOrDefault("responses", new HashMap<>());

            if (serviceRequestId == null || emailId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing required fields"));
            }

            System.out.println("📝 Processing questionnaire submission for SR: " + serviceRequestId);
            System.out.println("👤 From: " + emailId);
            System.out.println("📋 Responses: " + responses);

            // Process the questionnaire responses
            processQuestionnaireResponses(serviceRequestId, emailId, responses);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Questionnaire processed successfully");
            response.put("service_request_id", serviceRequestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error processing questionnaire: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    private void processQuestionnaireResponses(String serviceRequestId, String emailId, Map<String, Object> responses) {
        // Mock processing - would integrate with service tracker
        System.out.println("Processing responses for SR: " + serviceRequestId);
        
        for (Map.Entry<String, Object> entry : responses.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    // GET: /webhook/health
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "email-bot-webhook"));
    }
}
