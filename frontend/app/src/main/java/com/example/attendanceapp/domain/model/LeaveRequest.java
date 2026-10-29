package com.example.attendanceapp.domain.model;

public class LeaveRequest {
    private String id;
    private String userId;
    private ApprovalRequestType type;
    private String targetDate;        // Ngày xin nghỉ / Ngày đổi ca / Ngày điều chỉnh
    private String shiftId;           // ID ca làm muốn đổi (nếu có) / Tên ca
    private String reason;
    private boolean isEmergency;       // Nghỉ đột xuất
    private ApprovalStatus status;
    private long createdAt;
    private String checkInTime;       // Dành riêng cho điều chỉnh công
    private String checkOutTime;      // Dành riêng cho điều chỉnh công

    public LeaveRequest() {
    }

    public LeaveRequest(String id, String userId, ApprovalRequestType type, String targetDate,
                        String shiftId, String reason, boolean isEmergency,
                        ApprovalStatus status, long createdAt) {
        this(id, userId, type, targetDate, shiftId, reason, isEmergency, status, createdAt, null, null);
    }

    public LeaveRequest(String id, String userId, ApprovalRequestType type, String targetDate,
                        String shiftId, String reason, boolean isEmergency,
                        ApprovalStatus status, long createdAt, String checkInTime, String checkOutTime) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.targetDate = targetDate;
        this.shiftId = shiftId;
        this.reason = reason;
        this.isEmergency = isEmergency;
        this.status = status;
        this.createdAt = createdAt;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
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

    public ApprovalRequestType getType() {
        return type;
    }

    public void setType(ApprovalRequestType type) {
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

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
}
