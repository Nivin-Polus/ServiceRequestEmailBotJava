package com.servicerequest.emailbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalStorageService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.storage.directory:data}")
    private String storageDirectory;
    
    private static final String THREAD_MAPPING_FILE = "thread_sr_mapping.json";
    private static final String SESSION_DATA_FILE = "session_data.json";
    private static final String EMAIL_CACHE_FILE = "email_cache.json";
    
    public void saveThreadMapping(String conversationId, String srId, String sender, String subject) {
        try {
            ensureStorageDirectory();
            File file = new File(storageDirectory, THREAD_MAPPING_FILE);
            
            ObjectNode mappings;
            if (file.exists()) {
                mappings = (ObjectNode) objectMapper.readTree(file);
            } else {
                mappings = objectMapper.createObjectNode();
            }
            
            ObjectNode threadData = objectMapper.createObjectNode();
            threadData.put("sr_id", srId);
            threadData.put("sender", sender);
            threadData.put("subject", subject);
            threadData.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            mappings.set(conversationId, threadData);
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, mappings);
            System.out.println("Thread mapping saved locally: " + conversationId + " -> " + srId);
            
        } catch (IOException e) {
            System.err.println("Error saving thread mapping to local storage: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getThreadMapping(String conversationId) {
        try {
            File file = new File(storageDirectory, THREAD_MAPPING_FILE);
            if (!file.exists()) {
                return null;
            }
            
            JsonNode mappings = objectMapper.readTree(file);
            JsonNode threadData = mappings.get(conversationId);
            
            if (threadData != null) {
                Map<String, Object> mapping = new HashMap<>();
                mapping.put("sr_id", threadData.get("sr_id").asText());
                mapping.put("sender", threadData.get("sender").asText());
                mapping.put("subject", threadData.get("subject").asText());
                mapping.put("created_at", threadData.get("created_at").asText());
                return mapping;
            }
            
        } catch (IOException e) {
            System.err.println("Error reading thread mapping from local storage: " + e.getMessage());
        }
        return null;
    }
    
    public void saveSessionData(String key, Object data) {
        try {
            ensureStorageDirectory();
            File file = new File(storageDirectory, SESSION_DATA_FILE);
            
            ObjectNode sessionData;
            if (file.exists()) {
                sessionData = (ObjectNode) objectMapper.readTree(file);
            } else {
                sessionData = objectMapper.createObjectNode();
            }
            
            sessionData.set(key, objectMapper.valueToTree(data));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, sessionData);
            
        } catch (IOException e) {
            System.err.println("Error saving session data to local storage: " + e.getMessage());
        }
    }
    
    public <T> T getSessionData(String key, Class<T> valueType) {
        try {
            File file = new File(storageDirectory, SESSION_DATA_FILE);
            if (!file.exists()) {
                return null;
            }
            
            JsonNode sessionData = objectMapper.readTree(file);
            JsonNode data = sessionData.get(key);
            
            if (data != null) {
                return objectMapper.treeToValue(data, valueType);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading session data from local storage: " + e.getMessage());
        }
        return null;
    }
    
    public void saveEmailCache(String emailId, Object emailData) {
        try {
            ensureStorageDirectory();
            File file = new File(storageDirectory, EMAIL_CACHE_FILE);
            
            ObjectNode cache;
            if (file.exists()) {
                cache = (ObjectNode) objectMapper.readTree(file);
            } else {
                cache = objectMapper.createObjectNode();
            }
            
            cache.set(emailId, objectMapper.valueToTree(emailData));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
            
        } catch (IOException e) {
            System.err.println("Error saving email cache to local storage: " + e.getMessage());
        }
    }
    
    public <T> T getEmailCache(String emailId, Class<T> valueType) {
        try {
            File file = new File(storageDirectory, EMAIL_CACHE_FILE);
            if (!file.exists()) {
                return null;
            }
            
            JsonNode cache = objectMapper.readTree(file);
            JsonNode data = cache.get(emailId);
            
            if (data != null) {
                return objectMapper.treeToValue(data, valueType);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading email cache from local storage: " + e.getMessage());
        }
        return null;
    }
    
    private void ensureStorageDirectory() {
        File dir = new File(storageDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created storage directory: " + storageDirectory);
            }
        }
    }
    
    public void clearCache() {
        try {
            File emailCacheFile = new File(storageDirectory, EMAIL_CACHE_FILE);
            if (emailCacheFile.exists()) {
                boolean deleted = emailCacheFile.delete();
                if (deleted) {
                    System.out.println("Email cache cleared");
                }
            }
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
        }
    }
}
