package com.example.attendanceapp.domain.model;

public enum AdjustmentReasonType {
    MISSING_CHECK_IN("Thiếu check-in"),
    MISSING_CHECK_OUT("Thiếu check-out"),
    SUPPLEMENT_ATTENDANCE("Bổ sung công cả ngày"),
    SYSTEM_ERROR("Lỗi hệ thống/Máy chấm công");

    private final String label;

    AdjustmentReasonType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}