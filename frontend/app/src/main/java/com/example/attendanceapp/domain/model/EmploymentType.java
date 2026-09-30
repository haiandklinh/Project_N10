package com.example.attendanceapp.domain.model;

public enum EmploymentType {
    FULL_TIME("Toàn thời gian (Full-time)"),
    PART_TIME("Bán thời gian (Part-time)"),
    PROBATION("Thử việc"),
    INTERN("Thực tập");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}