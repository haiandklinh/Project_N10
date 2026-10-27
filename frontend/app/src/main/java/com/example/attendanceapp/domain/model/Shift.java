package com.example.attendanceapp.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class Shift {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final String id;
    private final String userId;        // Mã nhân viên sở hữu ca làm
    private final LocalDate date;
    private final String shiftName;     // "Ca Sáng", "Ca Chiều"
    private final LocalTime startTime;  // 08:00
    private final LocalTime endTime;    // 17:00
    private final String location;
    private final ShiftStatus status;
    private final LocalTime allowedCheckInTime;

    // Constructor tự sinh UUID cho ID
    public Shift(String userId, LocalDate date, String shiftName,
                 LocalTime startTime, LocalTime endTime, String location, ShiftStatus status) {
        this(UUID.randomUUID().toString(), userId, date, shiftName, startTime, endTime, location, status, null);
    }

    // Constructor day du (tuong thich cu)
    public Shift(String id, String userId, LocalDate date, String shiftName,
                 LocalTime startTime, LocalTime endTime, String location, ShiftStatus status) {
        this(id, userId, date, shiftName, startTime, endTime, location, status, null);
    }

    // Constructor day du kem allowedCheckInTime
    public Shift(String id, String userId, LocalDate date, String shiftName,
                 LocalTime startTime, LocalTime endTime, String location, ShiftStatus status,
                 LocalTime allowedCheckInTime) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = userId;
        this.date = date;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status != null ? status : ShiftStatus.PENDING;
        this.allowedCheckInTime = allowedCheckInTime != null ? allowedCheckInTime : startTime;
    }
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public String getShiftName() { return shiftName; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public ShiftStatus getStatus() { return status; }
    public LocalTime getAllowedCheckInTime() { return allowedCheckInTime != null ? allowedCheckInTime : startTime; }

    /** Getter động trả về chuỗi hiển thị giờ dạng "08:00 - 17:00" */
    public String getShiftTime() {
        if (startTime != null && endTime != null) {
            return startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
        }
        return "";
    }

    // =====================================================================
    // HELPER BUSINESS LOGIC METHODS
    // =====================================================================

    /** Kiểm tra xem ca làm việc này có phải là ca của ngày hôm nay không */
    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }

    /** Kiểm tra xem thời gian check-in hiện tại có bị trễ so với startTime không */
    public boolean isLateCheckIn(LocalTime checkInTime) {
        return checkInTime != null && startTime != null && checkInTime.isAfter(startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return Objects.equals(id, shift.id) &&
                Objects.equals(userId, shift.userId) &&
                Objects.equals(date, shift.date) &&
                Objects.equals(shiftName, shift.shiftName) &&
                Objects.equals(startTime, shift.startTime) &&
                Objects.equals(endTime, shift.endTime) &&
                Objects.equals(location, shift.location) &&
                status == shift.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, date, shiftName, startTime, endTime, location, status);
    }
}
