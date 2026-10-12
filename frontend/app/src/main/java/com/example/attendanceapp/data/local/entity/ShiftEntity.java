package com.example.attendanceapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.attendanceapp.domain.model.ShiftOption;

@Entity(tableName = "shifts")
public class ShiftEntity {
    @PrimaryKey
    @NonNull
    private final String id;
    private final String title;
    private final String startTime;
    private final String endTime;
    private final Double latitude;
    private final Double longitude;

    public ShiftEntity(@NonNull String id, String title, String startTime, String endTime, Double latitude, Double longitude) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public ShiftOption toDomain() {
        return new ShiftOption(id, title, startTime, endTime, latitude, longitude);
    }

    public static ShiftEntity fromDomain(ShiftOption shift) {
        return new ShiftEntity(
                shift.getId(),
                shift.getTitle(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getLatitude(),
                shift.getLongitude()
        );
    }
}
