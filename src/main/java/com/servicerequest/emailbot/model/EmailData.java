package com.servicerequest.emailbot.model;

public record EmailData(
    String messageId,
    String sender,
    String subject,
    String body,
    String conversationId,
    boolean hasAttachments
) {}
