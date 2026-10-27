package com.example.attendanceapp.domain.model;

import androidx.annotation.Nullable;

/**
 * Đại diện cho MỘT yêu cầu (nghỉ phép / điều chỉnh công / đổi ca) mà nhân viên
 * gửi lên, chờ Quản lý xử lý. Dùng cho ManagerApprovalFragment.
 */
public class ManagerApprovalItem {

    private final String id;
    private final String employeeName;
    private final String employeeAvatarUrl;
    private final String position;
    private final ApprovalRequestType type;
    private final String targetDateLabel;
    private final String detailLabel;
    private final String reason;
    private ApprovalStatus status;
    private final long createdAt;

    // Các trường dữ liệu phục vụ duyệt chuẩn xác
    @Nullable private final String currentShiftId;
    @Nullable private final String targetShiftId;
    @Nullable private final String workDay;

    public ManagerApprovalItem(String id, String employeeName, String employeeAvatarUrl, String position,
                               ApprovalRequestType type, String targetDateLabel, String detailLabel,
                               String reason, ApprovalStatus status, long createdAt) {
        this(id, employeeName, employeeAvatarUrl, position, type, targetDateLabel, detailLabel, reason, status, createdAt, null, null, null);
    }

    public ManagerApprovalItem(String id, String employeeName, String employeeAvatarUrl, String position,
                               ApprovalRequestType type, String targetDateLabel, String detailLabel,
                               String reason, ApprovalStatus status, long createdAt,
                               @Nullable String currentShiftId, @Nullable String targetShiftId, @Nullable String workDay) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeAvatarUrl = employeeAvatarUrl;
        this.position = position;
        this.type = type;
        this.targetDateLabel = targetDateLabel;
        this.detailLabel = detailLabel;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.currentShiftId = currentShiftId;
        this.targetShiftId = targetShiftId;
        this.workDay = workDay;
    }

    public String getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeAvatarUrl() { return employeeAvatarUrl; }
    public String getPosition() { return position; }
    public ApprovalRequestType getType() { return type; }
    public String getTargetDateLabel() { return targetDateLabel; }
    public String getDetailLabel() { return detailLabel; }
    public String getReason() { return reason; }
    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }

    @Nullable public String getCurrentShiftId() { return currentShiftId; }
    @Nullable public String getTargetShiftId() { return targetShiftId; }
    @Nullable public String getWorkDay() { return workDay; }
}
