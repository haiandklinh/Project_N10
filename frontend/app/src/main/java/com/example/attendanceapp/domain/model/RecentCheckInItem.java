package com.example.attendanceapp.domain.model;

public class RecentCheckInItem {
    private String id;
    private String employeeName;
    private String avatarUrl;
    private String shiftName;
    private String checkInTime;
    private AttendanceStatus status;

    public RecentCheckInItem(String id, String employeeName, String avatarUrl, String shiftName, String checkInTime, AttendanceStatus status) {
        this.id = id;
        this.employeeName = employeeName;
        this.avatarUrl = avatarUrl;
        this.shiftName = shiftName;
        this.checkInTime = checkInTime;
        this.status = status;
    }

    public String getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getShiftName() { return shiftName; }
    public String getCheckInTime() { return checkInTime; }
    public AttendanceStatus getStatus() { return status; }
}