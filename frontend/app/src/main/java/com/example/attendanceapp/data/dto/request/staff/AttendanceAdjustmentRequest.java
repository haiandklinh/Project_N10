package com.example.attendanceapp.data.dto.request.staff;

import com.google.gson.annotations.SerializedName;

public class AttendanceAdjustmentRequest {
    @SerializedName("workday")
    private String workday;

    @SerializedName("id_work_shift")
    private String workShiftId;

    @SerializedName("check_in_time")
    private String checkInTime;

    @SerializedName("check_out_time")
    private String checkOutTime;

    @SerializedName("reason")
    private String reason;

    public AttendanceAdjustmentRequest() {
    }

    public AttendanceAdjustmentRequest(String workday, String workShiftId, String checkInTime, String checkOutTime, String reason) {
        this.workday = workday;
        this.workShiftId = workShiftId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.reason = reason;
    }

    public String getWorkday() {
        return workday;
    }

    public void setWorkday(String workday) {
        this.workday = workday;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}