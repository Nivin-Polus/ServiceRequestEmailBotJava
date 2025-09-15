package com.servicerequest.emailbot.service;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class UserCredentials {
    
    public static Map<String, String> getCredentials(String emailId) {
        // Mock implementation - would retrieve from secure storage
        Map<String, String> credentials = new HashMap<>();
        credentials.put("AUTH_USERNAME", "mock_user");
        credentials.put("AUTH_PASSWORD", "mock_password");
        return credentials;
    }
}
