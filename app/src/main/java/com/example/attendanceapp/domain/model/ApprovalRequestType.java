package com.example.attendanceapp.domain.model;

import com.example.attendanceapp.R;

/**
 * Loại yêu cầu mà Quản lý cần xử lý trong màn "Duyệt yêu cầu".
 * Gộp từ các use case: Xử lý yêu cầu điều chỉnh chấm công, Xử lý yêu cầu nghỉ phép/đổi ca.
 */
public enum ApprovalRequestType {
    LEAVE("Nghỉ phép", R.drawable.ic_leave),
    ADJUSTMENT("Điều chỉnh công", R.drawable.ic_adjust),
    SWAP("Đổi ca", R.drawable.ic_refresh);

    private final String title;
    private final int iconResId;

    ApprovalRequestType(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
}
