package com.servicerequest.emailbot.service;

public class OutlookSession {
    private String authToken;
    private OutlookService service;

    public OutlookSession(String token) {
        this.authToken = token;
        this.service = new OutlookService();
    }

    public void updateAuthHeader(String newToken) {
        this.authToken = newToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public OutlookService getService() {
        return service;
    }
}
