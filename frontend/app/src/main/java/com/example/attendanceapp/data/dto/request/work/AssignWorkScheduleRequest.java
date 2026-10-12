package com.example.attendanceapp.data.dto.request.work;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AssignWorkScheduleRequest {
    @SerializedName("employee_ids")
    private List<String> employeeIds;

    @SerializedName("shift_id")
    private String shiftId;

    @SerializedName("work_date")
    private String workDate;

    @SerializedName("note")
    private String note;

    public AssignWorkScheduleRequest() {
    }

    public AssignWorkScheduleRequest(List<String> employeeIds, String shiftId, String workDate, String note) {
        this.employeeIds = employeeIds;
        this.shiftId = shiftId;
        this.workDate = workDate;
        this.note = note;
    }

    public List<String> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(List<String> employeeIds) {
        this.employeeIds = employeeIds;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
