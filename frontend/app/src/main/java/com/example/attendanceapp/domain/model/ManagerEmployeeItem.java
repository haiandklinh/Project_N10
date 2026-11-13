package com.example.attendanceapp.domain.model;

/**
 * Thông tin nhân viên hiển thị trong danh sách của Quản lý (tab "Nhân sự" và
 * tab "Xếp ca"). Gộp EmployeeProfile + vài số liệu tổng hợp phục vụ UI mock.
 */
public class ManagerEmployeeItem {

    private final String userId;
    private final String employeeCode; // "NV001"
    private final String fullName;
    private String avatarUrl;
    private final String position;
    private final String department;
    private String phoneNumber;
    private String joinDateLabel;
    private EmployeeStatus status; // Refactor: String -> EmployeeStatus
    private int shiftCountThisMonth;
    private int leaveCountThisMonth;
    private final long estimatedSalaryThisMonth;

    // Trường hỗ trợ riêng cho màn "Xếp ca": ca đã được xếp cho ngày đang chọn (có thể null)
    private String assignedShiftName;
    private String assignedShiftTime;

    public ManagerEmployeeItem(String userId, String employeeCode, String fullName, String avatarUrl,
                               String position, String department, String phoneNumber, String joinDateLabel,
                               EmployeeStatus status, int shiftCountThisMonth, int leaveCountThisMonth,
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
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getJoinDateLabel() { return joinDateLabel; }
    public void setJoinDateLabel(String joinDateLabel) { this.joinDateLabel = joinDateLabel; }
    public EmployeeStatus getStatus() { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }
    public int getShiftCountThisMonth() { return shiftCountThisMonth; }
    public void setShiftCountThisMonth(int shiftCountThisMonth) { this.shiftCountThisMonth = shiftCountThisMonth; }
    public int getLeaveCountThisMonth() { return leaveCountThisMonth; }
    public void setLeaveCountThisMonth(int leaveCountThisMonth) { this.leaveCountThisMonth = leaveCountThisMonth; }
    public long getEstimatedSalaryThisMonth() { return estimatedSalaryThisMonth; }

    public String getAssignedShiftName() { return assignedShiftName; }
    public void setAssignedShiftName(String assignedShiftName) { this.assignedShiftName = assignedShiftName; }
    public String getAssignedShiftTime() { return assignedShiftTime; }
    public void setAssignedShiftTime(String assignedShiftTime) { this.assignedShiftTime = assignedShiftTime; }

    private String payrollStatus;
    public String getPayrollStatus() { return payrollStatus; }
    public void setPayrollStatus(String payrollStatus) { this.payrollStatus = payrollStatus; }
}
