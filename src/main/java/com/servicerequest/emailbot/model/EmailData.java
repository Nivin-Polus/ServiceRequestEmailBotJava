package com.servicerequest.emailbot.model;

public class EmailData {
    private final String messageId;
    private final String sender;
    private final String subject;
    private final String body;
    private final String conversationId;
    private final boolean hasAttachments;

    public EmailData(String messageId, String sender, String subject, String body, String conversationId, boolean hasAttachments) {
        this.messageId = messageId;
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.conversationId = conversationId;
        this.hasAttachments = hasAttachments;
    }

    public String getId() {
        return messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getConversationId() {
        return conversationId;
    }

    public boolean hasAttachments() {
        return hasAttachments;
    }

    public boolean getHasAttachments() {
        return hasAttachments;
    }
}
