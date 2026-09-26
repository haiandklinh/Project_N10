package com.example.attendanceapp.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class RecentAttendance {

    private final LocalDate date;
    private final LocalTime checkIn;
    private final LocalTime checkOut;
    private final AttendanceStatus status;

    public RecentAttendance(LocalDate date, LocalTime checkIn, LocalTime checkOut, AttendanceStatus status) {
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getCheckIn() {
        return checkIn;
    }

    public LocalTime getCheckOut() {
        return checkOut;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
}