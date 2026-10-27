package com.example.attendanceapp.data.dto.response.shift;

import com.google.gson.annotations.SerializedName;

public class ShiftItemResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("allowed_check_in_time")
    private String allowedCheckInTime;

    @SerializedName(value = "check_in_radius", alternate = {"checkInRadius", "radius"})
    private double checkInRadius;

    @SerializedName(value = "longitude", alternate = {"lng", "lon"})
    private double longitude;

    @SerializedName(value = "latitude", alternate = {"lat"})
    private double latitude;

    @SerializedName("status")
    private String status;

    public ShiftItemResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
