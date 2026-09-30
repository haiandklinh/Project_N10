package com.example.attendanceapp.domain.model;

import java.util.Objects;

public class ShiftOption {
    private final String id;
    private final String title;
    private final String startTime; // "08:00"
    private final String endTime;   // "12:00"

    public ShiftOption(String id, String title, String startTime, String endTime) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    /** Getter động phục vụ hiển thị UI: "08:00 - 12:00" */
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftOption that = (ShiftOption) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}