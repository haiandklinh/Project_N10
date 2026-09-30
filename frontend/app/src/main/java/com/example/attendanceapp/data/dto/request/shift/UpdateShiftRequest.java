package com.example.attendanceapp.data.dto.request.shift;

import com.google.gson.annotations.SerializedName;

public class UpdateShiftRequest {

    @SerializedName("shiftName")
    private final String shiftName;

    @SerializedName("startTime")
    private final String startTime; // "HH:mm"

    @SerializedName("endTime")
    private final String endTime;   // "HH:mm"

    @SerializedName("description")
    private final String description;

    public UpdateShiftRequest(String shiftName, String startTime, String endTime, String description) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public UpdateShiftRequest(String shiftName, String startTime, String endTime) {
        this(shiftName, startTime, endTime, null);
    }

    public String getShiftName() {
        return shiftName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
}