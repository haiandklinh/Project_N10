package com.example.attendanceapp.data.dto.request.schedule;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AssignWorkScheduleRequest {
    @SerializedName("id_staff")
    private String employeeId;

    @SerializedName("work_day")
    private String workDate;

    @SerializedName("id_work_shift")
    private List<String> shiftIds;

    public AssignWorkScheduleRequest() {
    }

    public AssignWorkScheduleRequest(String employeeId, String workDate, List<String> shiftIds) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.shiftIds = shiftIds;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public List<String> getShiftIds() {
        return shiftIds;
    }

    public void setShiftIds(List<String> shiftIds) {
        this.shiftIds = shiftIds;
    }
}