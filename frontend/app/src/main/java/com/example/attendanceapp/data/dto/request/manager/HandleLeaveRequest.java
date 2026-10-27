package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class HandleLeaveRequest {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("id_work_shift_off")
    private String workShiftOffId;

    @SerializedName("workday_off")
    private String workDayOff;

    @SerializedName("status")
    private boolean status; // true: Đồng ý / Approved, false: Từ chối / Rejected

    @SerializedName("reason")
    private String reason;

    public HandleLeaveRequest() {
    }

    public HandleLeaveRequest(String staffId, String workShiftOffId, String workDayOff, boolean status, String reason) {
        this.staffId = staffId;
        this.workShiftOffId = workShiftOffId;
        this.workDayOff = workDayOff;
        this.status = status;
        this.reason = reason;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getWorkShiftOffId() {
        return workShiftOffId;
    }

    public void setWorkShiftOffId(String workShiftOffId) {
        this.workShiftOffId = workShiftOffId;
    }

    public String getWorkDayOff() {
        return workDayOff;
    }

    public void setWorkDayOff(String workDayOff) {
        this.workDayOff = workDayOff;
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