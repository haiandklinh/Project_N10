package com.example.attendanceapp.data.dto.request.shift;

import com.google.gson.annotations.SerializedName;

public class CreateShiftRequest {

    @SerializedName("name")
    private final String name;

    @SerializedName("start_time")
    private final String startTime;

    @SerializedName("end_time")
    private final String endTime;

    @SerializedName("latitude")
    private final Double latitude;

    @SerializedName("longitude")
    private final Double longitude;

    public CreateShiftRequest(String name, String startTime, String endTime, Double latitude, Double longitude) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getShiftName() { return name; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}