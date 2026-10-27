package com.example.attendanceapp.domain.model;

import androidx.annotation.AttrRes;
import com.example.attendanceapp.R;

public enum ShiftStatus {
    PENDING("Chưa vào ca", R.attr.colorAttendanceNotCheckedContainer, R.attr.colorAttendanceNotChecked),
    WORKING("Đang làm việc", R.attr.colorAttendancePresentContainer, R.attr.colorAttendancePresent),
    COMPLETED("Đã hoàn thành", R.attr.colorAttendancePresentContainer, R.attr.colorAttendancePresent),
    NOT_CHECKED_IN("Chưa chấm công", R.attr.colorAttendanceLateContainer, R.attr.colorAttendanceLate),
    ABSENT("Vắng mặt", R.attr.colorAttendanceAbsentContainer, R.attr.colorAttendanceAbsent);

    private final String displayName;
    @AttrRes private final int bgAttrRes;
    @AttrRes private final int textColorAttrRes;

    ShiftStatus(String displayName, @AttrRes int bgAttrRes, @AttrRes int textColorAttrRes) {
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