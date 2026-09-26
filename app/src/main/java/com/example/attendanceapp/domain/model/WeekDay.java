package com.example.attendanceapp.domain.model;

import java.time.LocalDate;

//Ngày:       01/09/2026
//Thứ:        Tuesday
//Chấm công:  PRESENT
//Hôm nay:    YES
public class WeekDay {
    private final LocalDate date;
    private final AttendanceStatus status;
    private final boolean today;

    public WeekDay(LocalDate date, AttendanceStatus status, boolean today) {
        this.date = date;
        this.status = status;
        this.today = today;
    }

    public LocalDate getDate() {
        return date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public boolean isToday() {
        return today;
    }


}