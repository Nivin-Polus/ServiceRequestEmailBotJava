package com.servicerequest.emailbot.service;

import org.springframework.stereotype.Service;

@Service
public class OutlookSessionService {
    
    private OutlookSession currentSession;

    public OutlookSession createSession(String token) {
        this.currentSession = new OutlookSession(token);
        return this.currentSession;
    }

    public void refreshToken(String newToken) {
        if (currentSession != null) {
            currentSession.updateAuthHeader(newToken);
        }
    }

    public OutlookSession getCurrentSession() {
        return currentSession;
    }
}
