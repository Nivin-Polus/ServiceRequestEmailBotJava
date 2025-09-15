package com.servicerequest.emailbot.service.outlook;

import org.springframework.stereotype.Component;

@Component
public class OutlookSession {
    private String authToken;
    private String refreshToken;
    private long tokenExpiryTime;
    
    public OutlookSession() {
        // Initialize session
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public long getTokenExpiryTime() {
        return tokenExpiryTime;
    }
    
    public void setTokenExpiryTime(long tokenExpiryTime) {
        this.tokenExpiryTime = tokenExpiryTime;
    }
    
    public boolean isTokenExpired() {
        return System.currentTimeMillis() > tokenExpiryTime;
    }
}
