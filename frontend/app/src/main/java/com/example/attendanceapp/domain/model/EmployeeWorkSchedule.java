package com.example.attendanceapp.domain.model;

import androidx.annotation.Nullable;

/**
 * Lịch làm việc của nhân viên (dùng cho Manager xem toàn bộ nhân viên)
 */
public class EmployeeWorkSchedule {
    private final String employeeId;
    private final String name;
    private final String position;
    private final String workDate;
    @Nullable private final String avatar;
    private final String status;
    private final ShiftInfo shift;

    public EmployeeWorkSchedule(String employeeId, String name, String position, String workDate,
                                String avatar, String status, ShiftInfo shift) {
        this.employeeId = employeeId;
        this.name = name;
        this.position = position;
        this.workDate = workDate;
        this.avatar = avatar;
        this.status = status;
        this.shift = shift;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getWorkDate() {
        return workDate;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public String getStatus() {
        return status;
    }

    public ShiftInfo getShift() {
        return shift;
    }

    public static class ShiftInfo {
        private final String shiftId;
        private final String shiftName;
        private final String startTime;
        private final String endTime;

        public ShiftInfo(String shiftId, String shiftName, String startTime, String endTime) {
            this.shiftId = shiftId;
            this.shiftName = shiftName;
            this.startTime = startTime;
            this.endTime = endTime;
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
    }
}
