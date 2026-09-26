package com.example.attendanceapp.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Locale;
import java.util.UUID;

public class HistoryAttendance {

    private final String id;
    private final String userId;
    private final LocalDate date;
    private final LocalTime checkIn;
    private final LocalTime checkOut;
    private final AttendanceStatus status;
    private final String location;

    public HistoryAttendance(
            LocalDate date,
            LocalTime checkIn,
            LocalTime checkOut,
            AttendanceStatus status,
            String location
    ) {
        this(UUID.randomUUID().toString(), "", date, checkIn, checkOut, status, location);
    }

    public HistoryAttendance(
            String id,
            String userId,
            LocalDate date,
            LocalTime checkIn,
            LocalTime checkOut,
            AttendanceStatus status,
            String location
    ) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.location = location;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public LocalTime getCheckIn() { return checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public AttendanceStatus getStatus() { return status; }
    public String getLocation() { return location; }

    /**
     * Tính tổng số giờ làm việc dạng số thực double (dùng cho logic tính toán/tổng kết)
     */
    public double getTotalWorkedHours() {
        if (checkIn == null || checkOut == null) {
            return 0.0;
        }

        Duration duration = Duration.between(checkIn, checkOut);

        // Xử lý trường hợp ca làm việc xuyên đêm (ví dụ 22:00 -> 06:00)
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }

        return duration.toMinutes() / 60.0;
    }

    /**
     * Trả về chuỗi định dạng rút gọn của tổng số giờ (ví dụ: "8.8h") dùng hiển thị UI
     */
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
        HistoryAttendance that = (HistoryAttendance) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(checkIn, that.checkIn) &&
                Objects.equals(checkOut, that.checkOut) &&
                status == that.status &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, date, checkIn, checkOut, status, location);
    }
}