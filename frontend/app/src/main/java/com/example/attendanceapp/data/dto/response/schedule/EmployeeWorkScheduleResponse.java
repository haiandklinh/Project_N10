package com.example.attendanceapp.data.dto.response.schedule;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class EmployeeWorkScheduleResponse {
    @SerializedName("employee_id")
    private String employeeId;

    @SerializedName("name")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("work_date")
    private String workDate;

    @Nullable
    @SerializedName("avatar")
    private String avatar;

    private String status;

    @SerializedName("shift")
    private ShiftInfo shift;

    public EmployeeWorkScheduleResponse() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ShiftInfo getShift() {
        return shift;
    }

    public void setShift(ShiftInfo shift) {
        this.shift = shift;
    }

    public static class ShiftInfo {
        @SerializedName("shift_id")
        private String shiftId;

        @SerializedName("shift_name")
        private String shiftName;

        @SerializedName("start_time")
        private String startTime;

        @SerializedName("end_time")
        private String endTime;

        public ShiftInfo() {
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
    }
}
