package com.example.attendanceapp.data.dto.request.attendance;

import com.google.gson.annotations.SerializedName;

public class CheckOutRequest {
    @SerializedName("id_work_schedule")
    private String workScheduleId;

    @SerializedName("check_out_time")
    private String checkOutTime;

    @SerializedName("latitude_check_out")
    private double latitudeCheckOut;

    @SerializedName("longitude_check_out")
    private double longitudeCheckOut;

    @SerializedName("workday")
    private String workday;

    public CheckOutRequest() {
    }

    public CheckOutRequest(String workScheduleId, String checkOutTime, double latitudeCheckOut, double longitudeCheckOut, String workday) {
        this.workScheduleId = workScheduleId;
        this.checkOutTime = checkOutTime;
        this.latitudeCheckOut = latitudeCheckOut;
        this.longitudeCheckOut = longitudeCheckOut;
        this.workday = workday;
    }

    public String getWorkScheduleId() { return workScheduleId; }
    public void setWorkScheduleId(String workScheduleId) { this.workScheduleId = workScheduleId; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public double getLatitudeCheckOut() { return latitudeCheckOut; }
    public void setLatitudeCheckOut(double latitudeCheckOut) { this.latitudeCheckOut = latitudeCheckOut; }

    public double getLongitudeCheckOut() { return longitudeCheckOut; }
    public void setLongitudeCheckOut(double longitudeCheckOut) { this.longitudeCheckOut = longitudeCheckOut; }

    public String getWorkday() { return workday; }
    public void setWorkday(String workday) { this.workday = workday; }
}