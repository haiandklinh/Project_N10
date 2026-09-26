package com.example.attendanceapp.domain.model;

/**
 * Đại diện cho MỘT yêu cầu (nghỉ phép / điều chỉnh công / đổi ca) mà nhân viên
 * gửi lên, chờ Quản lý xử lý. Dùng cho ManagerApprovalFragment.
 *
 * GHI CHÚ: Đây là model tổng hợp phục vụ hiển thị UI. Khi nối API thật, có thể
 * map trực tiếp từ LeaveRequest / AttendanceAdjustment kèm thông tin nhân viên.
 */
public class ManagerApprovalItem {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private final String id;
    private final String employeeName;
    private final String employeeAvatarUrl;
    private final String position;
    private final ApprovalRequestType type;
    private final String targetDateLabel;   // Ví dụ: "Ngày áp dụng: 20/09/2026"
    private final String detailLabel;       // Ví dụ: "08:00 - 17:00" hoặc "Đổi sang ca Chiều"
    private final String reason;
    private String status;
    private final long createdAt;

    public ManagerApprovalItem(String id, String employeeName, String employeeAvatarUrl, String position,
                                ApprovalRequestType type, String targetDateLabel, String detailLabel,
                                String reason, String status, long createdAt) {
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
    }

    public String getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeAvatarUrl() { return employeeAvatarUrl; }
    public String getPosition() { return position; }
    public ApprovalRequestType getType() { return type; }
    public String getTargetDateLabel() { return targetDateLabel; }
    public String getDetailLabel() { return detailLabel; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
}
