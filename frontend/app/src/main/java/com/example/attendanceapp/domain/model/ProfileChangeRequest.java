package com.example.attendanceapp.domain.model;

public class ProfileChangeRequest {
    private String id;
    private String userId;
    private ProfileFieldType fieldType; // Refactor: String -> ProfileFieldType
    private String oldValue;
    private String newValue;
    private String proofUrl;
    private ApprovalStatus status;      // Refactor: String -> ApprovalStatus
    private String rejectReason;
    private long createdAt;
    private long updatedAt;

    public ProfileChangeRequest() {
    }

    public ProfileChangeRequest(String id, String userId, ProfileFieldType fieldType,
                                String oldValue, String newValue, String proofUrl,
                                ApprovalStatus status, String rejectReason,
                                long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.fieldType = fieldType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.proofUrl = proofUrl;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProfileFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(ProfileFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getProofUrl() {
        return proofUrl;
    }

    public void setProofUrl(String proofUrl) {
        this.proofUrl = proofUrl;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}