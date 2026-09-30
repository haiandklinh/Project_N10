package com.example.attendanceapp.data.dto.request.work;

import com.google.gson.annotations.SerializedName;

public class WorkScheduleChangeRequest {
    @SerializedName("current_shift_id")
    private String currentShiftId;

    @SerializedName("target_shift_id")
    private String targetShiftId;

    @SerializedName("work_date")
    private String workDate;

    @SerializedName("reason")
    private String reason;

    public WorkScheduleChangeRequest() {}

    public WorkScheduleChangeRequest(String currentShiftId, String targetShiftId, String workDate, String reason) {
        this.currentShiftId = currentShiftId;
        this.targetShiftId = targetShiftId;
        this.workDate = workDate;
        this.reason = reason;
    }

    public String getCurrentShiftId() { return currentShiftId; }
    public void setCurrentShiftId(String currentShiftId) { this.currentShiftId = currentShiftId; }

    public String getTargetShiftId() { return targetShiftId; }
    public void setTargetShiftId(String targetShiftId) { this.targetShiftId = targetShiftId; }

    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
