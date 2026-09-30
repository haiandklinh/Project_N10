package com.example.attendanceapp.domain.model;

import androidx.annotation.ColorRes;
import com.example.attendanceapp.R;

public enum ApprovalStatus {
    PENDING("Chờ duyệt", R.color.attendance_late, R.color.attendance_late_container),
    APPROVED("Đã duyệt", R.color.attendance_present, R.color.attendance_present_container),
    REJECTED("Từ chối", R.color.attendance_absent, R.color.attendance_absent_container);

    private final String label;
    @ColorRes private final int textColorRes;
    @ColorRes private final int containerColorRes;

    ApprovalStatus(String label, @ColorRes int textColorRes, @ColorRes int containerColorRes) {
        this.label = label;
        this.textColorRes = textColorRes;
        this.containerColorRes = containerColorRes;
    }

    public String getLabel() { return label; }
    public int getTextColorRes() { return textColorRes; }
    public int getContainerColorRes() { return containerColorRes; }

    // Parse từ String trả về từ API (ví dụ: "PENDING", "APPROVED") sang Enum
    public static ApprovalStatus fromString(String status) {
        if (status == null) return PENDING;
        try {
            return ApprovalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // Mặc định nếu API trả về chuỗi lạ
        }
    }
}