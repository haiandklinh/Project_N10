package com.example.attendanceapp.data.dto.response.schedule;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EmployeeWorkScheduleResponse {
    @SerializedName("id_staff")
    private String employeeId;

    @SerializedName("name_staff")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("work_day")
    private String workDate;

    @Nullable
    @SerializedName("avatar")
    private String avatar;

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<ShiftInfo> shifts;

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

    public List<ShiftInfo> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftInfo> shifts) {
        this.shifts = shifts;
    }

    public static class ShiftInfo {
        @SerializedName("id")
        private String shiftId;

        @SerializedName("name")
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