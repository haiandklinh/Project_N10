package com.example.attendanceapp.domain.model;

import androidx.annotation.DrawableRes;
import com.example.attendanceapp.R;

/**
 * Trạng thái chấm công của nhân viên theo ngày.
 */
public enum AttendanceStatus {
    PRESENT("Đúng giờ", R.drawable.ic_check, R.attr.colorAttendancePresent, R.attr.colorAttendancePresentContainer),
    LATE("Đi muộn", R.drawable.ic_late, R.attr.colorAttendanceLate, R.attr.colorAttendanceLateContainer),
    EARLY_LEAVE("Về sớm", R.drawable.ic_warning, R.attr.colorAttendanceEarlyLeave, R.attr.colorAttendanceEarlyLeaveContainer),
    ABSENT("Vắng mặt", R.drawable.ic_close, R.attr.colorAttendanceAbsent, R.attr.colorAttendanceAbsentContainer),
    LEAVE("Nghỉ phép", R.drawable.ic_remove, R.attr.colorAttendanceLeave, R.attr.colorAttendanceLeaveContainer),
    NOT_CHECKED("Chưa chấm", R.drawable.ic_remove, R.attr.colorAttendanceNotChecked, R.attr.colorAttendanceNotCheckedContainer),
    WEEKEND("Nghỉ tuần", R.drawable.ic_remove, R.attr.colorAttendanceWeekend, R.attr.colorAttendanceWeekendContainer);

    private final String displayName;
    @DrawableRes
    private final int iconRes;
    private final int colorAttr;
    private final int containerColorAttr;

    AttendanceStatus(String displayName, @DrawableRes int iconRes, int colorAttr, int containerColorAttr) {
        this.displayName = displayName;
        this.iconRes = iconRes;
        this.colorAttr = colorAttr;
        this.containerColorAttr = containerColorAttr;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColorAttr() {
        return colorAttr;
    }

    public int getContainerColorAttr() {
        return containerColorAttr;
    }
}