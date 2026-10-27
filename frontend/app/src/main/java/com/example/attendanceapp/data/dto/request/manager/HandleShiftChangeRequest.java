package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class HandleShiftChangeRequest {
    @SerializedName(value = "id_staff", alternate = {"staff_id", "idStaff"})
    private String staffId;

    @SerializedName(value = "id_work_shift_cur", alternate = {"id_work_shift_current", "current_shift_id", "id_shift_current"})
    private String currentShiftId;

    @SerializedName(value = "id_work_shift_change", alternate = {"id_work_shift_target", "target_shift_id", "id_shift_change"})
    private String targetShiftId;

    @SerializedName(value = "workday", alternate = {"work_day", "date"})
    private String workDay;

    @SerializedName("status")
    private boolean status; // true: Đồng ý / Approved, false: Từ chối / Rejected

    @SerializedName("reason")
    private String reason;

    public HandleShiftChangeRequest() {
    }

    public HandleShiftChangeRequest(String staffId, String currentShiftId, String targetShiftId,
                                    String workDay, boolean status, String reason) {
        this.staffId = staffId;
        this.currentShiftId = currentShiftId;
        this.targetShiftId = targetShiftId;
        this.workDay = workDay;
        this.status = status;
        this.reason = reason;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getCurrentShiftId() {
        return currentShiftId;
    }

    public void setCurrentShiftId(String currentShiftId) {
        this.currentShiftId = currentShiftId;
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
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
