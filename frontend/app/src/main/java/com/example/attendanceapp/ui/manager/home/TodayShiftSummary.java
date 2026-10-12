package com.example.attendanceapp.ui.manager.home;

public class TodayShiftSummary {
    private String shiftName;
    private String timeRange;
    private String location;
    private String status;
    private int employeeCount;

    public TodayShiftSummary(String shiftName, String timeRange, String location, String status, int employeeCount) {
        this.shiftName = shiftName;
        this.timeRange = timeRange;
        this.location = location;
        this.status = status;
        this.employeeCount = employeeCount;
    }

    public String getShiftName() { return shiftName; }
    public String getTimeRange() { return timeRange; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public int getEmployeeCount() { return employeeCount; }
}