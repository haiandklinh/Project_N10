package com.example.attendanceapp.data.dto.response.staff;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StaffWorkScheduleResponse {
    @SerializedName(value = "workday", alternate = {"work_day", "date"})
    private String workDay;

    @SerializedName(value = "workshits", alternate = {"workshifts", "shifts", "data"})
    private List<ShiftInfo> shifts;

    public StaffWorkScheduleResponse() {
    }

    public StaffWorkScheduleResponse(String workDay, List<ShiftInfo> shifts) {
        this.workDay = workDay;
        this.shifts = shifts;
    }

    public String getWorkDay() {
        return workDay;
    }

    public void setWorkDay(String workDay) {
        this.workDay = workDay;
    }

    public List<ShiftInfo> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftInfo> shifts) {
        this.shifts = shifts;
    }

    public static class ShiftInfo {
        @SerializedName(value = "id_work_schedule", alternate = {"id", "workScheduleId", "schedule_id"})
        private String workScheduleId;

        @SerializedName(value = "id_shift", alternate = {"idShift", "shift_id", "shiftId", "id_work_shift", "work_shift_id"})
        private String shiftId;

        @SerializedName(value = "name_shift", alternate = {"name", "shiftName", "shift_name", "title"})
        private String shiftName;

        @SerializedName("start_time")
        private String startTime;

        @SerializedName("end_time")
        private String endTime;

        @SerializedName(value = "latitude", alternate = {"lat"})
        private Double latitude;

        @SerializedName(value = "longitude", alternate = {"lng", "lon"})
        private Double longitude;

        @SerializedName(value = "check_in_radius", alternate = {"checkInRadius", "radius"})
        private Double checkInRadius;

        @SerializedName(value = "allowed_check_in_time", alternate = {"allowedCheckInTime", "allowed_time"})
        private String allowedCheckInTime;

        public ShiftInfo() {
        }

        public ShiftInfo(String workScheduleId, String shiftName, String startTime, String endTime) {
            this.workScheduleId = workScheduleId;
            this.shiftName = shiftName;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getWorkScheduleId() {
            return workScheduleId;
        }

        public void setWorkScheduleId(String workScheduleId) {
            this.workScheduleId = workScheduleId;
        }

        public String getShiftId() {
            return shiftId;
        }

        public void setShiftId(String shiftId) {
            this.shiftId = shiftId;
        }

        public String getShiftName() {
            return shiftName;
        }

        public void setShiftName(String shiftName) {
            this.shiftName = shiftName;
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

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getCheckInRadius() {
            return checkInRadius;
        }

        public void setCheckInRadius(Double checkInRadius) {
            this.checkInRadius = checkInRadius;
        }

        public String getAllowedCheckInTime() {
            return allowedCheckInTime;
        }

        public void setAllowedCheckInTime(String allowedCheckInTime) {
            this.allowedCheckInTime = allowedCheckInTime;
        }
    }
}