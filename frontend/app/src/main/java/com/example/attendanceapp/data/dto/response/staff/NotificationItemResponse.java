package com.example.attendanceapp.data.dto.response.staff;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class NotificationItemResponse {
    @Nullable
    @SerializedName(value = "id", alternate = {"notification_id", "notificationId", "request_id"})
    private String id;

    @SerializedName("send_person")
    private String sendPerson;

    @SerializedName("company_name")
    private String companyName;

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

    public NotificationItemResponse(String sendPerson, String companyName, Map<String, Object> content, String sendAt, String notificationType) {
        this(null, sendPerson, companyName, content, sendAt, notificationType, null);
    }

    public NotificationItemResponse(String id, String sendPerson, String companyName, Map<String, Object> content, String sendAt, String notificationType, String status) {
        this.id = id;
        this.sendPerson = sendPerson;
        this.companyName = companyName;
        this.content = content;
        this.sendAt = sendAt;
        this.notificationType = notificationType;
        this.status = status;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public String getSendPerson() {
        return sendPerson;
    }

    public void setSendPerson(String sendPerson) {
        this.sendPerson = sendPerson;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public void setStatus(@Nullable String status) {
        this.status = status;
    }
}
