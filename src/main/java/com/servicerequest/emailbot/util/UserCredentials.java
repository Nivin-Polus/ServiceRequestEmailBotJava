package com.servicerequest.emailbot.util;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserCredentials {
    private final Map<String, String> credentials;
    
    public UserCredentials() {
        this.credentials = new HashMap<>();
        loadCredentials();
    }
    
    private void loadCredentials() {
        // Load credentials from environment variables
        credentials.put("outlook.client.id", System.getenv("OUTLOOK_CLIENT_ID"));
        credentials.put("outlook.client.secret", System.getenv("OUTLOOK_CLIENT_SECRET"));
        credentials.put("outlook.tenant.id", System.getenv("OUTLOOK_TENANT_ID"));
        credentials.put("claude.api.key", System.getenv("CLAUDE_API_KEY"));
        credentials.put("auth.username", System.getenv("AUTH_USERNAME"));
        credentials.put("auth.password", System.getenv("AUTH_PASSWORD"));
        credentials.put("slack.bot.token", System.getenv("SLACK_BOT_TOKEN"));
    }
    
    public String getCredential(String key) {
        return credentials.get(key);
    }
    
    public boolean hasCredential(String key) {
        String value = credentials.get(key);
        return value != null && !value.trim().isEmpty();
    }
    
    public void validateRequiredCredentials() throws RuntimeException {
        String[] required = {
            "outlook.client.id",
            "outlook.client.secret", 
            "outlook.tenant.id",
            "claude.api.key"
        };
        
        for (String key : required) {
            if (!hasCredential(key)) {
                throw new RuntimeException("Required credential missing: " + key);
            }
        }
    }
}
