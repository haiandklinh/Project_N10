package com.example.attendanceapp.domain.model;

import androidx.annotation.AttrRes;
import com.example.attendanceapp.R;

/**
 * Các loại thông báo trong app.
 */
public enum NotificationType {
    HUMAN("NHÂN SỰ", R.attr.colorAttendanceLeaveContainer, R.attr.colorAttendanceLeave),
    PAYROLL("BẢNG LƯƠNG", R.attr.colorAttendanceLateContainer, R.attr.colorAttendanceLate),
    LEAVE("NGHỈ PHÉP", R.attr.colorAttendanceAbsentContainer, R.attr.colorAttendanceAbsent),
    SCHEDULE("LỊCH TRÌNH", R.attr.colorAttendanceWeekendContainer, R.attr.colorAttendanceWeekend),
    ATTENDANCE("CHẤM CÔNG", R.attr.colorAttendanceNotCheckedContainer, R.attr.colorAttendanceNotChecked),
    SYSTEM("HỆ THỐNG", R.attr.colorAttendancePresentContainer, R.attr.colorAttendancePresent),
    AUTH("XÁC THỰC", R.attr.colorAttendanceEarlyLeaveContainer, R.attr.colorAttendanceEarlyLeave);

    private final String displayName;
    @AttrRes private final int bgAttrRes;
    @AttrRes private final int textColorAttrRes;

    NotificationType(String displayName, @AttrRes int bgAttrRes, @AttrRes int textColorAttrRes) {
        this.displayName = displayName;
        this.bgAttrRes = bgAttrRes;
        this.textColorAttrRes = textColorAttrRes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBgAttrRes() {
        return bgAttrRes;
    }

    public int getTextColorAttrRes() {
        return textColorAttrRes;
    }
}
