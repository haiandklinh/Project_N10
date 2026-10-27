package com.example.attendanceapp.data.dto.response.manager;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class NotificationItemResponse {

    @SerializedName(value = "id_staff", alternate = {"staff_id", "idStaff", "staffId"})
    private String staffId;

    @Nullable
    @SerializedName(value = "id", alternate = {"notification_id", "notificationId"})
    private String notificationId;

    @Nullable
    @SerializedName(value = "avatar_send_person", alternate = {"avatar_staff", "staff_avatar", "send_person_avatar", "avatar", "avatar_url", "avatarUrl", "image", "photo"})
    private String sendPersonAvatar;

    @Nullable
    @SerializedName(value = "phone", alternate = {"phoneNumber", "phone_number", "sdt"})
    private String phoneNumber;

    @SerializedName(value = "send_person", alternate = {"sendPerson", "sender_name", "senderName", "name_staff", "staff_name", "full_name", "fullName", "name"})
    private String sendPerson;

    @SerializedName("content")
    private Map<String, Object> content;

    @SerializedName("send_at")
    private String sendAt;

    @SerializedName("notification_type")
    private String notificationType;

    @Nullable
    @SerializedName("status")
    private String status;

    public NotificationItemResponse() {
    }

    public NotificationItemResponse(String staffId, String sendPersonAvatar, String sendPerson,
                                    Map<String, Object> content, String sendAt, String notificationType) {
        this(staffId, sendPersonAvatar, sendPerson, content, sendAt, notificationType, null);
    }

    public NotificationItemResponse(String staffId, String sendPersonAvatar, String sendPerson,
                                    Map<String, Object> content, String sendAt, String notificationType, String status) {
        this.staffId = staffId;
        this.sendPersonAvatar = sendPersonAvatar;
        this.sendPerson = sendPerson;
        this.content = content;
        this.sendAt = sendAt;
        this.notificationType = notificationType;
        this.status = status;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    @Nullable
    public String getSendPersonAvatar() {
        // 1. Ưu tiên Base64 hoặc URL HTTP từ content (do Staff gửi trực tiếp kèm trong join request)
        if (content != null) {
            String[] keys = {"avatar_staff", "staff_avatar", "avatar", "avatar_url", "avatarUrl", "avatar_send_person", "send_person_avatar", "image", "photo"};
            for (String key : keys) {
                Object val = content.get(key);
                if (val != null) {
                    String str = val.toString().trim();
                    if (!str.isEmpty() && !str.equalsIgnoreCase("null") && !str.equalsIgnoreCase("none")) {
                        if (str.startsWith("data:image") || str.startsWith("http://") || str.startsWith("https://")
                                || (!str.startsWith("file://") && !str.startsWith("/") && str.length() > 200)) {
                            return str;
                        }
                    }
                }
            }
        }

        // 2. Tiếp theo kiểm tra sendPersonAvatar (trả về từ bảng staffs)
        if (sendPersonAvatar != null && !sendPersonAvatar.trim().isEmpty()
                && !sendPersonAvatar.equalsIgnoreCase("null") && !sendPersonAvatar.equalsIgnoreCase("none")) {
            return sendPersonAvatar.trim();
        }

        // 3. Fallback đọc giá trị bất kỳ từ content
        if (content != null) {
            String[] keys = {"avatar_staff", "staff_avatar", "avatar", "avatar_url", "avatarUrl", "avatar_send_person", "send_person_avatar", "image", "photo"};
            for (String key : keys) {
                Object val = content.get(key);
                if (val != null) {
                    String str = val.toString().trim();
                    if (!str.isEmpty() && !str.equalsIgnoreCase("null") && !str.equalsIgnoreCase("none")) {
                        return str;
                    }
                }
            }
        }
        return null;
    }

    public void setSendPersonAvatar(String sendPersonAvatar) {
        this.sendPersonAvatar = sendPersonAvatar;
    }

    public String getSendPerson() {
        if (sendPerson != null && !sendPerson.trim().isEmpty()) {
            return sendPerson.trim();
        }
        if (content != null) {
            String[] keys = {"full_name", "fullName", "fullname", "name", "send_person", "name_staff", "staff_name", "sender_name"};
            for (String key : keys) {
                Object val = content.get(key);
                if (val != null && !val.toString().trim().isEmpty()) {
                    return val.toString().trim();
                }
            }
        }
        return sendPerson;
    }

    @Nullable
    public String getPhoneNumber() {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            return phoneNumber.trim();
        }
        if (content != null) {
            String[] keys = {"phone", "phone_number", "phoneNumber", "sdt"};
            for (String key : keys) {
                Object val = content.get(key);
                if (val != null && !val.toString().trim().isEmpty()) {
                    return val.toString().trim();
                }
            }
        }
        return "";
    }

    public void setPhoneNumber(@Nullable String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSendPerson(String sendPerson) {
        this.sendPerson = sendPerson;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public String getSendAt() {
        return sendAt;
    }

    public void setSendAt(String sendAt) {
        this.sendAt = sendAt;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
