package com.example.attendanceapp.data.dto.request.shift;

import com.google.gson.annotations.SerializedName;

public class UpdateShiftRequest {

    @SerializedName("shiftName")
    private final String shiftName;

    @SerializedName("startTime")
    private final String startTime;

    @SerializedName("endTime")
    private final String endTime;

    @SerializedName("latitude")
    private final Double latitude;

    @SerializedName("longitude")
    private final Double longitude;

    public UpdateShiftRequest(String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getShiftName() { return shiftName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}