package com.example.attendanceapp.data.dto.request.work;

import com.google.gson.annotations.SerializedName;

public class AssignWorkScheduleRequest {
    @SerializedName("employee_id")
    private String employeeId;

    @SerializedName("name")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("shift_id")
    private String shiftId;

    @SerializedName("work_date")
    private String workDate;

    public AssignWorkScheduleRequest() {
    }

    public AssignWorkScheduleRequest(String employeeId, String name, String position, String shiftId, String workDate) {
        this.employeeId = employeeId;
        this.name = name;
        this.position = position;
        this.shiftId = shiftId;
        this.workDate = workDate;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }
}
