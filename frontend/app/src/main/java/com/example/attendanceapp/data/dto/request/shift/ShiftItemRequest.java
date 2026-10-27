package com.example.attendanceapp.data.dto.request.shift;

import com.google.gson.annotations.SerializedName;

public class ShiftItemRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("allowed_check_in_time")
    private String allowedCheckInTime;

    @SerializedName("check_in_radius")
    private double checkInRadius;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("latitude")
    private double latitude;

    public ShiftItemRequest() {
    }

    public ShiftItemRequest(String name, String startTime, String endTime,
                            String allowedCheckInTime, double checkInRadius,
                            double longitude, double latitude) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.allowedCheckInTime = allowedCheckInTime;
        this.checkInRadius = checkInRadius;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAllowedCheckInTime() {
        return allowedCheckInTime;
    }

    public void setAllowedCheckInTime(String allowedCheckInTime) {
        this.allowedCheckInTime = allowedCheckInTime;
    }

    public double getCheckInRadius() {
        return checkInRadius;
    }

    public void setCheckInRadius(double checkInRadius) {
        this.checkInRadius = checkInRadius;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}