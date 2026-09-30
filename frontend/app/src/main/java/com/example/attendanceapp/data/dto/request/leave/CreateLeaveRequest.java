package com.example.attendanceapp.data.dto.request.leave;

import com.google.gson.annotations.SerializedName;

public class CreateLeaveRequest {
    @SerializedName("shift_id")
    private String shiftId;

    @SerializedName("work_date")
    private String workDate;

    @SerializedName("reason")
    private String reason;

    public CreateLeaveRequest() {}

    public CreateLeaveRequest(String shiftId, String workDate, String reason) {
        this.shiftId = shiftId;
        this.workDate = workDate;
        this.reason = reason;
    }

    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }

    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
