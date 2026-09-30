package com.example.attendanceapp.domain.model;

import androidx.annotation.ColorRes;
import com.example.attendanceapp.R;

public enum EmployeeStatus {
    ACTIVE("Đang làm việc", R.color.attendance_present, R.color.attendance_present_container),
    ON_LEAVE("Đang nghỉ phép", R.color.attendance_leave, R.color.attendance_leave_container),
    TERMINATED("Đã nghỉ việc", R.color.attendance_absent, R.color.attendance_absent_container);

    private final String label;
    @ColorRes private final int textColorRes;
    @ColorRes private final int containerColorRes;

    EmployeeStatus(String label, @ColorRes int textColorRes, @ColorRes int containerColorRes) {
        this.label = label;
        this.textColorRes = textColorRes;
        this.containerColorRes = containerColorRes;
    }

    public String getLabel() { return label; }
    public int getTextColorRes() { return textColorRes; }
    public int getContainerColorRes() { return containerColorRes; }

    public static EmployeeStatus fromString(String status) {
        if (status == null) return ACTIVE;
        try {
            return EmployeeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}