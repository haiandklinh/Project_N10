package com.example.attendanceapp.domain.model;

public class LeaveRequest {
    private String id;
    private String userId;
    private String type;        // "LEAVE" (Nghỉ phép), "SWAP" (Đổi ca)
    private String targetDate;  // Ngày xin nghỉ / Ngày đổi ca
    private String shiftId;     // ID ca làm muốn đổi (nếu có)
    private String reason;
    private boolean isEmergency; // Nghỉ đột xuất
    private String status;      // PENDING, APPROVED, REJECTED
    private long createdAt;

    public LeaveRequest() {
    }

    public LeaveRequest(String id, String userId, String type, String targetDate, String shiftId, String reason, boolean isEmergency, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.targetDate = targetDate;
        this.shiftId = shiftId;
        this.reason = reason;
        this.isEmergency = isEmergency;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}