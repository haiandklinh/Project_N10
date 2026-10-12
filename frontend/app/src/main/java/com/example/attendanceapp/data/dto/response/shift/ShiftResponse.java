package com.example.attendanceapp.data.dto.response.shift;

import com.example.attendanceapp.domain.model.ShiftOption;
import com.google.gson.annotations.SerializedName;

public class ShiftResponse {

    @SerializedName("shiftId")
    private String shiftId;

    @SerializedName("shiftName")
    private String shiftName;

    @SerializedName("startTime")
    private String startTime; // Format: "HH:mm" (e.g. "08:00")

    @SerializedName("endTime")
    private String endTime;   // Format: "HH:mm" (e.g. "17:00")

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;



    public ShiftResponse() {
    }

    public ShiftResponse(String shiftId, String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getShiftId() {
        return shiftId;
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

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    /**
     * Mapper chuyển đổi từ DTO Response sang Domain Model ShiftOption
     */
    public ShiftOption toDomain() {
        return new ShiftOption(
                shiftId,
                shiftName,
                startTime,
                endTime,
                latitude,
                longitude
        );
    }
}