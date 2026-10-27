package com.example.attendanceapp.data.dto.response.attendance;

import com.google.gson.annotations.SerializedName;

public class AttendanceRecordItemResponse {
    @SerializedName(value = "check_in_time", alternate = {"checkInTime", "check_in"})
    private Object checkInTime; // Có thể là String hoặc Long tuỳ server

    @SerializedName(value = "check_out_time", alternate = {"checkOutTime", "check_out"})
    private Object checkOutTime;

    @SerializedName(value = "workday", alternate = {"work_day", "date"})
    private String workday;

    @SerializedName(value = "name_shift", alternate = {"shift_name", "shiftName", "name"})
    private String shiftName;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "name_staff", alternate = {"staff_name", "employee_name", "name"})
    private String staffName;

    @SerializedName(value = "id_staff", alternate = {"staff_id", "employee_id"})
    private String staffId;

    @SerializedName(value = "avatar", alternate = {"avatar_url"})
    private String avatar;

    public AttendanceRecordItemResponse() {
    }

    public Object getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Object checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckInTimeString() {
        if (checkInTime == null) return null;
        return checkInTime.toString();
    }

    public Object getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Object checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getCheckOutTimeString() {
        if (checkOutTime == null) return null;
        return checkOutTime.toString();
    }

    public String getWorkday() { return workday; }
    public void setWorkday(String workday) { this.workday = workday; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
