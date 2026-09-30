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

    @SerializedName("description")
    private String description;

    public ShiftResponse() {
    }

    public ShiftResponse(String shiftId, String shiftName, String startTime, String endTime, String description) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    /**
     * Mapper chuyển đổi từ DTO Response sang Domain Model ShiftOption
     */
    public ShiftOption toDomain() {
        return new ShiftOption(
                shiftId,
                shiftName,
                startTime,
                endTime
        );
    }
}