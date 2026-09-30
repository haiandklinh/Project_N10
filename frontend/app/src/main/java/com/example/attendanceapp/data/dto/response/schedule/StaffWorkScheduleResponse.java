package com.example.attendanceapp.data.dto.response.schedule;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StaffWorkScheduleResponse {
    @SerializedName("work_date")
    private String workDate;

    @SerializedName("shifts")
    private List<ShiftInfo> shifts;

    public StaffWorkScheduleResponse() {
    }

    public StaffWorkScheduleResponse(String workDate, List<ShiftInfo> shifts) {
        this.workDate = workDate;
        this.shifts = shifts;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public List<ShiftInfo> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftInfo> shifts) {
        this.shifts = shifts;
    }

    public static class ShiftInfo {
        @SerializedName("shift_name")
        private String shiftName;

        @SerializedName("start_time")
        private String startTime;

        @SerializedName("end_time")
        private String endTime;

        public ShiftInfo() {
        }

        public ShiftInfo(String shiftName, String startTime, String endTime) {
            this.shiftName = shiftName;
            this.startTime = startTime;
            this.endTime = endTime;
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
