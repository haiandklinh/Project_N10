package com.example.attendanceapp.domain.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.example.attendanceapp.R;

public enum DayStatus {
    NONE(0, 0),
    SUCCESS(R.drawable.ic_check, R.color.attendance_present),
    WARNING(R.drawable.ic_warning, R.color.attendance_late),
    CRITICAL(R.drawable.ic_warning, R.color.attendance_absent);

    @DrawableRes
    private final int iconRes;

    @ColorRes
    private final int colorRes;

    DayStatus(@DrawableRes int iconRes, @ColorRes int colorRes) {
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColorRes() {
        return colorRes;
    }
}