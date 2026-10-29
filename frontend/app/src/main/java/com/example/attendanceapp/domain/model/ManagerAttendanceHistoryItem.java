package com.example.attendanceapp.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ManagerAttendanceHistoryItem {
    private final String id;
    private final String staffId;
    private final String staffName;
    private final String avatar;
    private final LocalDate date;
    private final String shiftName;
    private final LocalTime checkIn;
    private final LocalTime checkOut;
    private AttendanceStatus status;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;

    public ManagerAttendanceHistoryItem(
            String id,
            String staffId,
            String staffName,
            String avatar,
            LocalDate date,
            String shiftName,
            LocalTime checkIn,
            LocalTime checkOut,
            AttendanceStatus status,
            LocalTime startTime,
            LocalTime endTime,
            String location
    ) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.staffId = staffId != null ? staffId : "";
        this.staffName = staffName != null ? staffName : "Nhân viên";
        this.avatar = avatar;
        this.date = date;
        this.shiftName = shiftName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status != null ? status : AttendanceStatus.NOT_CHECKED;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public String getId() { return id; }
    public String getStaffId() { return staffId; }
    public String getStaffName() { return staffName; }
    public String getAvatar() { return avatar; }
    public LocalDate getDate() { return date; }
    public String getShiftName() { return shiftName; }
    public LocalTime getCheckIn() { return checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getLocation() { return location; }

    public double getTotalWorkedHours() {
        if (checkIn == null || checkOut == null) {
            return 0.0;
        }
        Duration duration = Duration.between(checkIn, checkOut);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        return duration.toMinutes() / 60.0;
    }

    public String getShortFormattedTotalTime() {
        if (checkIn == null || checkOut == null) {
            return "--:--";
        }
        double hours = getTotalWorkedHours();
        if (hours <= 0) return "--:--";
        return String.format(Locale.US, "%.1fh", hours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerAttendanceHistoryItem that = (ManagerAttendanceHistoryItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(staffId, that.staffId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(shiftName, that.shiftName) &&
                Objects.equals(checkIn, that.checkIn) &&
                Objects.equals(checkOut, that.checkOut) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, staffId, date, shiftName, checkIn, checkOut, status);
    }
}
