package com.servicerequest.emailbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalStorageService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String dataDirectory = "data";
    
    public LocalStorageService() {
        // Create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(dataDirectory));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }
    
    /**
     * Load thread to service request mapping from JSON file
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> loadThreadSRMapping() {
        String filePath = dataDirectory + "/thread_sr_mapping.json";
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(file, HashMap.class);
        } catch (IOException e) {
            System.err.println("Failed to load thread SR mapping: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Save thread to service request mapping to JSON file
     */
    public void saveThreadSRMapping(Map<String, String> mapping) {
        String filePath = dataDirectory + "/thread_sr_mapping.json";
        
        try {
            objectMapper.writeValue(new File(filePath), mapping);
        } catch (IOException e) {
            System.err.println("Failed to save thread SR mapping: " + e.getMessage());
        }
    }
    
    /**
     * Load session data from JSON file
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadSessionData() {
        String filePath = dataDirectory + "/session_data.json";
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(file, HashMap.class);
        } catch (IOException e) {
            System.err.println("Failed to load session data: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Save session data to JSON file
     */
    public void saveSessionData(Map<String, Object> sessionData) {
        String filePath = dataDirectory + "/session_data.json";
        
        try {
            objectMapper.writeValue(new File(filePath), sessionData);
        } catch (IOException e) {
            System.err.println("Failed to save session data: " + e.getMessage());
        }
    }
    
    /**
     * Load email cache from JSON file
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadEmailCache() {
        String filePath = dataDirectory + "/email_cache.json";
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(file, HashMap.class);
        } catch (IOException e) {
            System.err.println("Failed to load email cache: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Save email cache to JSON file
     */
    public void saveEmailCache(Map<String, Object> emailCache) {
        String filePath = dataDirectory + "/email_cache.json";
        
        try {
            objectMapper.writeValue(new File(filePath), emailCache);
        } catch (IOException e) {
            System.err.println("Failed to save email cache: " + e.getMessage());
        }
    }
    
    /**
     * Get thread mapping for a specific conversation ID
     */
    public Map<String, Object> getThreadMapping(String conversationId) {
        Map<String, String> allMappings = loadThreadSRMapping();
        
        if (allMappings.containsKey(conversationId)) {
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("srId", allMappings.get(conversationId));
            threadInfo.put("conversationId", conversationId);
            return threadInfo;
        }
        
        return null;
    }
    
    /**
     * Save thread mapping with conversation details
     */
    public void saveThreadMapping(String conversationId, String srId, String sender, String subject) {
        Map<String, String> currentMappings = loadThreadSRMapping();
        currentMappings.put(conversationId, srId);
        saveThreadSRMapping(currentMappings);
        
        // Also save detailed thread information
        Map<String, Object> threadDetails = new HashMap<>();
        threadDetails.put("srId", srId);
        threadDetails.put("sender", sender);
        threadDetails.put("subject", subject);
        threadDetails.put("timestamp", System.currentTimeMillis());
        
        // Save to session data for additional context
        Map<String, Object> sessionData = loadSessionData();
        sessionData.put("thread_" + conversationId, threadDetails);
        saveSessionData(sessionData);
    }
}
