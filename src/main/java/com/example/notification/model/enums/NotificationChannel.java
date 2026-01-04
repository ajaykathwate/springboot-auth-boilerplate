package com.example.notification.model.enums;

/**
 * Supported notification channels for the multi-channel notification service.
 */
public enum NotificationChannel {
    EMAIL("email", ".html"),
    SMS("sms", ".txt"),
    WHATSAPP("whatsapp", ".txt"),
    PUSH("push", ".json"),
    IN_APP("inapp", ".html");

    private final String folderName;
    private final String templateExtension;

    NotificationChannel(String folderName, String templateExtension) {
        this.folderName = folderName;
        this.templateExtension = templateExtension;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getTemplateExtension() {
        return templateExtension;
    }

    public String getQueueName() {
        return "notification." + folderName + ".queue";
    }

    public String getRoutingKey() {
        return "notification." + folderName;
    }
}
