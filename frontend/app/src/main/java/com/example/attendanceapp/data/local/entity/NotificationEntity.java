package com.example.attendanceapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    @ColumnInfo(name = "role")
    private String role;

    @Ignore
    public NotificationEntity(
            @NonNull String id,
            String title,
            String message,
            String createdAt,
            String type,
            boolean isRead
    ) {
        this(id, title, message, createdAt, type, isRead, null);
    }

    public NotificationEntity(
            @NonNull String id,
            String title,
            String message,
            String createdAt,
            String type,
            boolean isRead,
            String role
    ) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.type = type;
        this.isRead = isRead;
        this.role = role;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ==================== DOMAIN MAPPING ====================

    public Notification toDomain() {
        NotificationType notifType = parseNotificationType(type);
        LocalDateTime time = parseDateTime(createdAt);

        return new Notification(
                id,
                title != null ? title.trim() : "",
                message != null ? message.trim() : "",
                time,
                notifType,
                isRead
        );
    }

    public static NotificationEntity fromDomain(
            Notification notification,
            String role
    ) {
        String timeStr = notification.getCreatedAt() != null
                ? notification.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                : LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        String typeStr = notification.getType() != null
                ? notification.getType().name()
                : NotificationType.SYSTEM.name();

        String safeTitle = notification.getTitle() != null ? notification.getTitle().trim() : "";
        String safeMessage = notification.getMessage() != null ? notification.getMessage().trim() : "";

        return new NotificationEntity(
                notification.getId(),
                safeTitle,
                safeMessage,
                timeStr,
                typeStr,
                notification.isRead(),
                role
        );
    }

    public static NotificationEntity fromDomain(Notification notification) {
        return fromDomain(notification, null);
    }

    // ==================== NOTIFICATION TYPE ====================

    private static NotificationType parseNotificationType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return NotificationType.SYSTEM;
        }

        try {
            return NotificationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotificationType.SYSTEM;
        }
    }

    // ==================== DATE TIME PARSING ====================

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        String time = value.trim();

        // ISO 8601: 2026-11-13T10:30:00
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }

        // ISO 8601 có timezone: 2026-11-13T10:30:00+07:00
        try {
            return OffsetDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        // dd/MM/yyyy HH:mm
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (DateTimeParseException ignored) {
        }

        // yyyy-MM-dd HH:mm:ss
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }

        return LocalDateTime.now();
    }
}