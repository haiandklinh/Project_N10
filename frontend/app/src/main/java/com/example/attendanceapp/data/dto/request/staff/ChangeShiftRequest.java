package com.example.attendanceapp.data.dto.request.staff;

import com.google.gson.annotations.SerializedName;

public class ChangeShiftRequest {
    @SerializedName(value = "id_work_shift_cur", alternate = {"id_work_shift_current", "current_shift_id", "id_shift_current", "shift_id_current"})
    private String currentShiftId;

    @SerializedName("id_work_shift_current")
    private String currentShiftIdAlias;

    @SerializedName(value = "id_work_shift_change", alternate = {"id_work_shift_target", "target_shift_id", "id_shift_change", "shift_id_change"})
    private String targetShiftId;

    @SerializedName(value = "workday", alternate = {"work_day", "date"})
    private String workDay;

    @SerializedName("work_day")
    private String workDayAlias;

    @SerializedName("reason")
    private String reason;

    public ChangeShiftRequest() {
    }

    public ChangeShiftRequest(String currentShiftId, String targetShiftId, String workDay, String reason) {
        this.currentShiftId = currentShiftId;
        this.currentShiftIdAlias = currentShiftId;
        this.targetShiftId = targetShiftId;
        this.workDay = workDay;
        this.workDayAlias = workDay;
        this.reason = reason;
    }

    public String getCurrentShiftId() {
        return currentShiftId;
    }

    public void setCurrentShiftId(String currentShiftId) {
        this.currentShiftId = currentShiftId;
        this.currentShiftIdAlias = currentShiftId;
    }

    public String getTargetShiftId() {
        return targetShiftId;
    }

    public void setTargetShiftId(String targetShiftId) {
        this.targetShiftId = targetShiftId;
    }

    public String getWorkDay() {
        return workDay;
    }

    public void setWorkDay(String workDay) {
        this.workDay = workDay;
        this.workDayAlias = workDay;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
