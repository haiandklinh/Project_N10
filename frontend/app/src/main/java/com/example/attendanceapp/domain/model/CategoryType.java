package com.example.attendanceapp.domain.model;

import com.example.attendanceapp.R;

public enum CategoryType {
    CHECK_IN("Chấm công", R.drawable.ic_check_circle),
    ADJUSTMENT("Điều chỉnh", R.drawable.ic_adjust),
    SWAP_SHIFT("Đổi ca", R.drawable.ic_refresh),
    LEAVE_REQUEST("Đơn từ", R.drawable.ic_form),
    PAYROLL("Bảng lương", R.drawable.ic_payroll),
    STATISTIC("Thống kê", R.drawable.ic_chart);

    private final String title;
    private final int iconResId;

    CategoryType(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
}