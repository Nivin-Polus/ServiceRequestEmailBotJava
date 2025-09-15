package com.servicerequest.emailbot.service.outlook;

import org.springframework.stereotype.Service;

@Service
public class OutlookSessionService {
    private OutlookSession currentSession;
    
    public OutlookSessionService() {
        this.currentSession = new OutlookSession();
    }
    
    public OutlookSession getCurrentSession() {
        return currentSession;
    }
    
    public void setCurrentSession(OutlookSession session) {
        this.currentSession = session;
    }
    
    public boolean isSessionValid() {
        return currentSession != null && !currentSession.isTokenExpired();
    }
    
    public void refreshSession() {
        // Implementation for session refresh
        if (currentSession != null && currentSession.isTokenExpired()) {
            // Refresh token logic here
        }
    }
}
