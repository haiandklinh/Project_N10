package com.example.attendanceapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    public NotificationEntity(@NonNull String id, String title, String message, String createdAt, String type, boolean isRead) {
        this(id, title, message, createdAt, type, isRead, null);
    }

    public NotificationEntity(@NonNull String id, String title, String message, String createdAt, String type, boolean isRead, String role) {
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

    public Notification toDomain() {
        NotificationType notifType = NotificationType.SYSTEM;
        if (type != null) {
            try {
                notifType = NotificationType.valueOf(type);
            } catch (IllegalArgumentException ignored) {
            }
        }

        LocalDateTime time = LocalDateTime.now();
        if (createdAt != null) {
            try {
                time = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {
                try {
                    time = LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                } catch (Exception ignored2) {
                    try {
                        time = LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception ignored3) {
                    }
                }
            }
        }

        return new Notification(
                id,
                sanitizeText(title),
                sanitizeText(message),
                time,
                notifType,
                isRead
        );
    }

    public static NotificationEntity fromDomain(Notification notification, String role) {
        String timeStr = notification.getCreatedAt() != null
                ? notification.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                : LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        String typeStr = notification.getType() != null
                ? notification.getType().name()
                : NotificationType.SYSTEM.name();

        return new NotificationEntity(
                notification.getId(),
                sanitizeText(notification.getTitle()),
                sanitizeText(notification.getMessage()),
                timeStr,
                typeStr,
                notification.isRead(),
                role
        );
    }

    public static NotificationEntity fromDomain(Notification notification) {
        return fromDomain(notification, null);
    }

    /**
     * Tự động làm sạch và sửa chữa các chuỗi thông báo bị lỗi font chữ hoặc encoding (mojibake / cp1252 / ? / \uFFFD).
     */
    public static String sanitizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 1. Nếu chuỗi bị dính lỗi mã hóa UTF-8 lồng Windows-1252 (mojibake)
        if (text.contains("Ă") || text.contains("Ä") || text.contains("áº") || text.contains("á»")
                || text.contains("Ã") || text.contains("Æ°") || text.contains("Æ¡") || text.contains("Â")) {
            try {
                byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
                String candidate = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                if (!candidate.contains("\uFFFD")) {
                    text = candidate;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Sửa các chuỗi bị lỗi dấu hỏi/mất ký tự do javac Cp1252 trước đây
        if (text.contains("?") || text.contains("\uFFFD")) {
            String lower = text.toLowerCase();
            // Tiêu đề/Nội dung chào mừng
            if (lower.contains("attendance app") || lower.contains("ch?o m?ng") || lower.contains("chào m?ng") || lower.contains("cho m?ng")) {
                if (lower.contains("trải nghiệm") || lower.contains("tr?i nghi?m") || lower.contains("user")) {
                    return "Chào mừng và chúc user trải nghiệm trên Attendance App tốt!";
                }
                return "Chào mừng đến với Attendance App";
            }
            // Đổi ca
            if (lower.contains("?i ca") || lower.contains("đổi ca")) {
                if (lower.contains("k?t qu?") || lower.contains("kết quả")) {
                    return "Kết quả yêu cầu đổi ca";
                }
                if (lower.contains("ph?n h?i") || lower.contains("phản hồi")) {
                    return "Quản lý đã phản hồi yêu cầu đổi ca làm việc.";
                }
                if (lower.contains("duy?t") || lower.contains("duyệt") || lower.contains("đã duy")) {
                    return "Yêu cầu đổi ca đã duyệt";
                }
                if (lower.contains("yêu cầu") || lower.contains("yu c?u")) {
                    return "Yêu cầu đổi ca làm việc";
                }
            }
            // Nghỉ phép
            if (lower.contains("ngh? php") || lower.contains("nghỉ phép") || lower.contains("ngh?")) {
                if (lower.contains("k?t qu?") || lower.contains("kết quả")) {
                    return "Kết quả đơn xin nghỉ phép";
                }
                if (lower.contains("ph?n h?i") || lower.contains("phản hồi")) {
                    return "Quản lý đã phản hồi đơn xin nghỉ phép của bạn.";
                }
                if (lower.contains("duy?t") || lower.contains("duyệt") || lower.contains("đã duy")) {
                    return "Đơn xin nghỉ phép đã duyệt";
                }
                return "Đơn xin nghỉ phép mới";
            }
            // Điều chỉnh chấm công
            if ((lower.contains("ch?m cng") || lower.contains("chấm công"))
                    && (lower.contains("i?u ch?nh") || lower.contains("điều chỉnh"))) {
                if (lower.contains("k?t qu?") || lower.contains("kết quả")) {
                    return "Kết quả điều chỉnh chấm công";
                }
                if (lower.contains("ph?n h?i") || lower.contains("phản hồi")) {
                    return "Quản lý đã phản hồi yêu cầu điều chỉnh chấm công.";
                }
                if (lower.contains("duy?t") || lower.contains("duyệt") || lower.contains("đã duy")) {
                    return "Điều chỉnh chấm công đã duyệt";
                }
                return "Yêu cầu điều chỉnh chấm công";
            }
            // Gia nhập công ty
            if (lower.contains("gia nh?p") || lower.contains("gia nhập") || lower.contains("tham gia")) {
                if (lower.contains("k?t qu?") || lower.contains("kết quả")) {
                    return "Kết quả yêu cầu gia nhập";
                }
                if (lower.contains("t? ch?i") || lower.contains("từ chối")) {
                    return "Yêu cầu gia nhập bị từ chối";
                }
                if (lower.contains("duy?t") || lower.contains("duyệt") || lower.contains("đã duy")) {
                    return "Yêu cầu gia nhập đã được duyệt";
                }
                if (lower.contains("chúc mừng") || lower.contains("chc m?ng") || lower.contains("ph duy?t")) {
                    return "Chúc mừng! Quản lý đã phê duyệt yêu cầu gia nhập công ty của bạn.";
                }
                if (lower.contains("rất tiếc") || lower.contains("r?t ti?c")) {
                    return "Rất tiếc! Yêu cầu gia nhập công ty của bạn đã bị từ chối.";
                }
                return "Yêu cầu tham gia công ty";
            }
            // Lịch làm việc
            if (lower.contains("l?ch lm vi?c") || lower.contains("lịch làm việc") || lower.contains("x?p l?ch") || lower.contains("xếp lịch")) {
                if (lower.contains("quản lý") || lower.contains("qu?n l?")) {
                    return "Quản lý đã xếp lịch làm việc mới cho bạn.";
                }
                return "Lịch làm việc mới";
            }
            // Nhắc nhở chấm công
            if (lower.contains("?n gi? ch?m cng") || lower.contains("đến giờ chấm công")) {
                return "Đến giờ chấm công";
            }
            if (lower.contains("nh?c nh?") || lower.contains("nhắc nhở")) {
                if (lower.contains("15")) {
                    return "Nhắc nhở chấm công (Quá 15p)";
                }
                if (lower.contains("30")) {
                    return "Cảnh báo chấm công (Quá 30p)";
                }
                return "Nhắc nhở chấm công";
            }
            if (lower.contains("c?nh bo") || lower.contains("cảnh báo")) {
                return "Cảnh báo chấm công (Quá 30p)";
            }
            // Thông báo mới
            if (lower.contains("thng bo m?i") || lower.contains("thông báo")) {
                if (lower.contains("hệ thống") || lower.contains("h? th?ng")) {
                    return "Bạn có thông báo mới từ hệ thống.";
                }
                return "Thông báo mới";
            }

            // Dọn dẹp ký tự \uFFFD đơn lẻ nếu còn sót lại
            text = text.replace("\uFFFD", "");
        }

        return text;
    }
}
