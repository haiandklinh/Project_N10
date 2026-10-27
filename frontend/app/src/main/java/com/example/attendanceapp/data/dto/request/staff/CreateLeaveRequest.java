package com.example.attendanceapp.data.dto.request.staff;
import com.google.gson.annotations.SerializedName;

public class CreateLeaveRequest {
    @SerializedName("work_day_off")
    private String workDayOff;

    @SerializedName("id_work_shift_off")
    private String workShiftOffId;

    @SerializedName("reason")
    private String reason;

    public CreateLeaveRequest() {
    }

    public CreateLeaveRequest(String workDayOff, String workShiftOffId, String reason) {
        this.workDayOff = workDayOff;
        this.workShiftOffId = workShiftOffId;
        this.reason = reason;
    }

    public String getWorkDayOff() {
        return workDayOff;
    }

    public void setWorkDayOff(String workDayOff) {
        this.workDayOff = workDayOff;
    }

    public String getWorkShiftOffId() {
        return workShiftOffId;
    }

    public void setWorkShiftOffId(String workShiftOffId) {
        this.workShiftOffId = workShiftOffId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}