package com.example.attendanceapp.ui.manager.home;

/**
 * Một tác vụ nhanh hiển thị trên Dashboard của Quản lý (Xếp ca, Duyệt đơn, Nhân sự...).
 */
public class ManagerQuickAction {
    private final String title;
    private final int iconResId;
    private final Runnable onClick;

    public ManagerQuickAction(String title, int iconResId, Runnable onClick) {
        this.title = title;
        this.iconResId = iconResId;
        this.onClick = onClick;
    }

    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
    public void performClick() { if (onClick != null) onClick.run(); }
}
