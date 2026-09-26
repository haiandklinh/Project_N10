package com.example.attendanceapp.domain.model;

/**
 * Thông tin nhân viên hiển thị trong danh sách của Quản lý (tab "Nhân sự" và
 * tab "Xếp ca"). Gộp EmployeeProfile + vài số liệu tổng hợp phục vụ UI mock.
 */
public class ManagerEmployeeItem {

    public static final String STATUS_ACTIVE = "ACTIVE";       // Đang làm
    public static final String STATUS_ON_LEAVE = "ON_LEAVE";   // Đang nghỉ phép

    private final String userId;
    private final String employeeCode; // "NV001"
    private final String fullName;
    private final String avatarUrl;
    private final String position;
    private final String department;
    private final String phoneNumber;
    private final String joinDateLabel;
    private final String status;
    private final int shiftCountThisMonth;
    private final int leaveCountThisMonth;
    private final long estimatedSalaryThisMonth;

    // Trường hỗ trợ riêng cho màn "Xếp ca": ca đã được xếp cho ngày đang chọn (có thể null)
    private String assignedShiftName;
    private String assignedShiftTime;

    public ManagerEmployeeItem(String userId, String employeeCode, String fullName, String avatarUrl,
                                String position, String department, String phoneNumber, String joinDateLabel,
                                String status, int shiftCountThisMonth, int leaveCountThisMonth,
                                long estimatedSalaryThisMonth) {
        this.userId = userId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.position = position;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.joinDateLabel = joinDateLabel;
        this.status = status;
        this.shiftCountThisMonth = shiftCountThisMonth;
        this.leaveCountThisMonth = leaveCountThisMonth;
        this.estimatedSalaryThisMonth = estimatedSalaryThisMonth;
    }

    public String getUserId() { return userId; }
    public String getEmployeeCode() { return employeeCode; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getJoinDateLabel() { return joinDateLabel; }
    public String getStatus() { return status; }
    public int getShiftCountThisMonth() { return shiftCountThisMonth; }
    public int getLeaveCountThisMonth() { return leaveCountThisMonth; }
    public long getEstimatedSalaryThisMonth() { return estimatedSalaryThisMonth; }

    public String getAssignedShiftName() { return assignedShiftName; }
    public void setAssignedShiftName(String assignedShiftName) { this.assignedShiftName = assignedShiftName; }
    public String getAssignedShiftTime() { return assignedShiftTime; }
    public void setAssignedShiftTime(String assignedShiftTime) { this.assignedShiftTime = assignedShiftTime; }
}
