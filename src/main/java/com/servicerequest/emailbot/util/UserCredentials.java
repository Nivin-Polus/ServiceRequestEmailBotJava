package com.servicerequest.emailbot.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;

public class UserCredentials {
    private Map<String, String> credentials;
    private final Dotenv dotenv;
    
    public UserCredentials() {
        this.credentials = new HashMap<>();
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        loadCredentials();
    }
    
    private void loadCredentials() {
        // Load credentials from environment variables
        credentials.put("outlook.client.id", dotenv.get("OUTLOOK_CLIENT_ID"));
        credentials.put("outlook.client.secret", dotenv.get("OUTLOOK_CLIENT_SECRET"));
        credentials.put("outlook.tenant.id", dotenv.get("OUTLOOK_TENANT_ID"));
        credentials.put("claude.api.key", dotenv.get("CLAUDE_API_KEY"));
        credentials.put("auth.username", dotenv.get("AUTH_USERNAME"));
        credentials.put("auth.password", dotenv.get("AUTH_PASSWORD"));
        credentials.put("slack.bot.token", dotenv.get("SLACK_BOT_TOKEN"));
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
