package com.example.attendanceapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance_table", indices = {@Index("date")})
public class AttendanceRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String date;       // "yyyy-MM-dd"
    public long checkInTime;  // milliseconds
    public long checkOutTime; // milliseconds
    public String shiftName;
    public double latitude;   // Vĩ độ
    public double longitude;  // Kinh độ

    public String status;     // "PRESENT", "ABSENT", "LATE", "WEEKEND"
}