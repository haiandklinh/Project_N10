package com.example.attendanceapp.domain.model;

/**
 * GIẢ ĐỊNH: các loại thông báo trong app. Nếu đã có enum tương tự ở nơi khác
 * trong project, xoá file này và dùng lại enum thật, đồng thời sửa NotificationAdapter
 * cho khớp.
 */
public enum NotificationType {
    HUMAN,
    PAYROLL,        // Bảng lương
    LEAVE,          // Nghỉ phép / Khiếu nại
    SCHEDULE,       // Ca làm việc
    ATTENDANCE,     // Nhắc nhở chấm công
    SYSTEM,         // Thông báo hệ thống chung
    AUTH            // Xác thực / OTP
}