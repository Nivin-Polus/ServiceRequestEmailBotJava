package com.servicerequest.emailbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceRequestId {
    // Mapping Email thread IDs to Service Request IDs
    private static Map<String, Map<String, Object>> EMAIL_THREAD_SR_MAPPING = new HashMap<>();
    private static final String MAPPING_FILE = System.getenv().getOrDefault("THREAD_MAPPING_FILE", "thread_sr_mapping.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    // Load thread-to-SR mappings from JSON file
    public static void loadMappings() {
        File file = new File(MAPPING_FILE);
        if (file.exists()) {
            try {
                EMAIL_THREAD_SR_MAPPING = mapper.readValue(file, new TypeReference<Map<String, Map<String, Object>>>() {});
                System.out.println("📂 Loaded " + EMAIL_THREAD_SR_MAPPING.size() + " thread mappings from " + MAPPING_FILE);
            } catch (Exception e) {
                System.out.println("❌ Error loading mappings: " + e.getMessage());
                EMAIL_THREAD_SR_MAPPING = new HashMap<>();
            }
        } else {
            System.out.println("📂 No existing mapping file found. Starting with empty mappings.");
            EMAIL_THREAD_SR_MAPPING = new HashMap<>();
        }
    }

    // Save thread-to-SR mappings to JSON file
    public static void saveMappings() {
        try {
            mapper.writeValue(new File(MAPPING_FILE), EMAIL_THREAD_SR_MAPPING);
            System.out.println("💾 Saved " + EMAIL_THREAD_SR_MAPPING.size() + " thread mappings to " + MAPPING_FILE);
        } catch (IOException e) {
            System.out.println("❌ Error saving mappings: " + e.getMessage());
        }
    }

    // Check if thread already has a Service Request
    public static boolean isExistingThread(String threadId) {
        return EMAIL_THREAD_SR_MAPPING.containsKey(threadId);
    }

    // Get Service Request data for a thread
    public static Map<String, Object> getSRByThread(String threadId) {
        return EMAIL_THREAD_SR_MAPPING.getOrDefault(threadId, Collections.emptyMap());
    }

    // Save thread-to-SR mapping
    public static void saveThreadSRMapping(String threadId, String srId, String sender, String subject) {
        Map<String, Object> srData = new HashMap<>();
        srData.put("sr_id", srId);
        srData.put("sender", sender);
        srData.put("subject", subject);
        srData.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        EMAIL_THREAD_SR_MAPPING.put(threadId, srData);
        saveMappings(); // Auto-save after each update
        
        System.out.printf("💾 Saved thread mapping: %s -> SR %s%n", threadId, srId);
    }

    // Get all mappings (for debugging)
    public static Map<String, Map<String, Object>> getAllMappings() {
        return new HashMap<>(EMAIL_THREAD_SR_MAPPING);
    }

    // Clear all mappings (for testing)
    public static void clearAllMappings() {
        EMAIL_THREAD_SR_MAPPING.clear();
        saveMappings();
        System.out.println("🗑️ Cleared all thread mappings");
    }
}
