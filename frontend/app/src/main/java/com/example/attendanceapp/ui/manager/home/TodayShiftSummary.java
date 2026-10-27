package com.example.attendanceapp.ui.manager.home;

import com.example.attendanceapp.domain.model.ShiftStatus;

public class TodayShiftSummary {
    private String shiftName;
    private String timeRange;
    private String location;
    private String status;
    private int employeeCount;
    private ShiftStatus shiftStatus;

    public TodayShiftSummary(String shiftName, String timeRange, String location, String status, int employeeCount) {
        this(shiftName, timeRange, location, status, employeeCount, ShiftStatus.PENDING);
    }

    public TodayShiftSummary(String shiftName, String timeRange, String location, String status, int employeeCount, ShiftStatus shiftStatus) {
        this.shiftName = shiftName;
        this.timeRange = timeRange;
        this.location = location;
        this.status = status;
        this.employeeCount = employeeCount;
        this.shiftStatus = shiftStatus;
    }

    public String getShiftName() { return shiftName; }
    public String getTimeRange() { return timeRange; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public int getEmployeeCount() { return employeeCount; }
    public ShiftStatus getShiftStatus() { return shiftStatus; }
}
