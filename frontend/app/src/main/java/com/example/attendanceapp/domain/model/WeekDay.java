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
    private final boolean inCurrentMonth;

    public WeekDay(LocalDate date, AttendanceStatus status, boolean today, boolean inCurrentMonth) {
        this.date = date;
        this.status = status;
        this.today = today;
        this.inCurrentMonth = inCurrentMonth;
    }

    public WeekDay(LocalDate date, AttendanceStatus status, boolean today) {
        this(date, status, today, true);
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

    public boolean isInCurrentMonth() {
        return inCurrentMonth;
    }


}