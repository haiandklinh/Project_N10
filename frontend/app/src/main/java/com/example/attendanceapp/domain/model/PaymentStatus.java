package com.example.attendanceapp.domain.model;

import androidx.annotation.ColorRes;
import com.example.attendanceapp.R;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán", R.color.attendance_absent, R.color.attendance_absent_container),
    PAID("Đã thanh toán", R.color.attendance_present, R.color.attendance_present_container),
    PROCESSING("Đang xử lý", R.color.attendance_late, R.color.attendance_late_container);

    private final String label;
    @ColorRes private final int textColorRes;
    @ColorRes private final int containerColorRes;

    PaymentStatus(String label, @ColorRes int textColorRes, @ColorRes int containerColorRes) {
        this.label = label;
        this.textColorRes = textColorRes;
        this.containerColorRes = containerColorRes;
    }

    public String getLabel() { return label; }
    public int getTextColorRes() { return textColorRes; }
    public int getContainerColorRes() { return containerColorRes; }
}