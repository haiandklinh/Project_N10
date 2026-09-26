package com.example.attendanceapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "shift_table", indices = {@Index("name")})
public class ShiftEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;              // "Ca Sáng", "Ca Chiều"
    public long startTime;           // milliseconds (08:00)
    public long endTime;             // milliseconds (17:00)
    public long checkInAllowTime;    // milliseconds (07:45)
    public long checkOutAllowTime;   // milliseconds (17:15)
    public double latitude;         // Vĩ độ địa điểm làm việc
    public double longitude;        // Kinh độ địa điểm làm việc
    public String location;          // Tên địa điểm
    public String status;            // "ACTIVE", "INACTIVE"
}