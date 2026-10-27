package com.example.attendanceapp.domain.model;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;

import com.example.attendanceapp.R;

public enum DayStatus {
    NONE(0, 0),
    SUCCESS(R.drawable.ic_check, R.attr.colorAttendancePresent),
    WARNING(R.drawable.ic_warning, R.attr.colorAttendanceLate),
    CRITICAL(R.drawable.ic_warning, R.attr.colorAttendanceAbsent);

    @DrawableRes
    private final int iconRes;

    @AttrRes
    private final int colorAttrRes;

    DayStatus(@DrawableRes int iconRes, @AttrRes int colorAttrRes) {
        this.iconRes = iconRes;
        this.colorAttrRes = colorAttrRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColorAttrRes() {
        return colorAttrRes;
    }
}
