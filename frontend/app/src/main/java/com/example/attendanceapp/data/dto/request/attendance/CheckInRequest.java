package com.example.attendanceapp.data.dto.request.attendance;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {
    @SerializedName("id_work_schedule")
    private String workScheduleId;

    @SerializedName("check_in_time")
    private String checkInTime;

    @SerializedName("latitude_check_in")
    private double latitudeCheckIn;

    @SerializedName("longitude_check_in")
    private double longitudeCheckIn;

    @SerializedName("workday")
    private String workday;

    public CheckInRequest() {
    }

    public CheckInRequest(String workScheduleId, String checkInTime, double latitudeCheckIn, double longitudeCheckIn, String workday) {
        this.workScheduleId = workScheduleId;
        this.checkInTime = checkInTime;
        this.latitudeCheckIn = latitudeCheckIn;
        this.longitudeCheckIn = longitudeCheckIn;
        this.workday = workday;
    }

    public String getWorkScheduleId() { return workScheduleId; }
    public void setWorkScheduleId(String workScheduleId) { this.workScheduleId = workScheduleId; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public double getLatitudeCheckIn() { return latitudeCheckIn; }
    public void setLatitudeCheckIn(double latitudeCheckIn) { this.latitudeCheckIn = latitudeCheckIn; }

    public double getLongitudeCheckIn() { return longitudeCheckIn; }
    public void setLongitudeCheckIn(double longitudeCheckIn) { this.longitudeCheckIn = longitudeCheckIn; }

    public String getWorkday() { return workday; }
    public void setWorkday(String workday) { this.workday = workday; }
}