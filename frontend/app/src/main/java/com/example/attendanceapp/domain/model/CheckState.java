package com.example.attendanceapp.domain.model;

import java.time.LocalTime;

public enum CheckState {
    NOT_CHECKED_IN("Chấm công ngay", true),
    CHECKED_IN("Check-out ngay", true),
    CHECKED_OUT("Đã hoàn thành công", false);

    private final String buttonText;
    private final boolean isButtonEnabled;

    CheckState(String buttonText, boolean isButtonEnabled) {
        this.buttonText = buttonText;
        this.isButtonEnabled = isButtonEnabled;
    }

    public String getButtonText() { return buttonText; }
    public boolean isButtonEnabled() { return isButtonEnabled; }

    // Hàm tự xác định trạng thái dựa trên giờ Check-in/Check-out
    public static CheckState from(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkInTime == null) return NOT_CHECKED_IN;
        if (checkOutTime == null) return CHECKED_IN;
        return CHECKED_OUT;
    }
}