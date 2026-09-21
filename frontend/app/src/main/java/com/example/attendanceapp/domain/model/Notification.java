package com.example.attendanceapp.domain.model;

import java.time.LocalDateTime;

public class Notification {

    private final String id;
    private final String title;
    private final String message;
    private final LocalDateTime createdAt;
    private final NotificationType type;
    private boolean read;

    public Notification(
            String id,
            String title,
            String message,
            LocalDateTime createdAt,
            NotificationType type,
            boolean read
    ) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.type = type;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}