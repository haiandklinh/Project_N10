package com.example.attendanceapp.core.notification;

import androidx.annotation.Nullable;

import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.domain.model.UserRole;

import java.util.Locale;

/**
 * Quy tắc phân quyền hiển thị thông báo theo vai trò (MANAGER / EMPLOYEE).
 * Dùng chung cho cả WebSocket realtime và đồng bộ API để đảm bảo thông báo
 * không bị hiển thị chéo vai trò.
 */
public final class NotificationRolePolicy {

    public enum Audience {
        MANAGER_ONLY,
        EMPLOYEE_ONLY,
        BOTH
    }

    private NotificationRolePolicy() {
    }

    /**
     * Xác định đối tượng được phép nhận theo mã loại thông báo từ server.
     */
    public static Audience audienceForRawType(@Nullable String rawType) {
        if (rawType == null || rawType.trim().isEmpty()) {
            return Audience.BOTH;
        }
        String normalized = rawType.trim().toUpperCase(Locale.US);
        switch (normalized) {
            case "JOIN_COMPANY_REQUEST":
            case "CHANGE_WORK_SHIFT":
            case "SHIFT_CHANGE_REQUEST":
            case "CHANGE_WORK_SHIFT_REQUEST":
            case "NEW_CHANGE_REQUEST":
            case "WORK_SCHEDULE_REQUEST":
            case "CHANGE_LEAVE_REQUEST":
            case "LEAVE_REQUEST":
            case "ATTENDANCE_ADJUSTMENT_REQUEST":
                return Audience.MANAGER_ONLY;

            case "ASSIGN_WORK_SCHEDULE":
            case "SCHEDULE_ASSIGNED":
            case "WORK_SCHEDULE_ASSIGNED":
            case "SCHEDULE_UPDATED":
            case "SCHEDULE":
            case "JOIN_COMPANY_REQUEST_RESPONSE":
            case "CHANGE_WORK_SHIFT_RESPONSE":
            case "SHIFT_CHANGE_APPROVED":
            case "CHANGE_LEAVE_REQUEST_RESPONSE":
            case "LEAVE_APPROVED":
            case "ATTENDANCE_ADJUSTMENT_RESPONSE":
            case "ATTENDANCE_ADJUSTMENT_APPROVED":
            case "PAYROLL":
            case "REMINDER":
                return Audience.EMPLOYEE_ONLY;

            default:
                if (normalized.endsWith("_RESPONSE")
                        || normalized.endsWith("_APPROVED")
                        || normalized.endsWith("_ASSIGNED")) {
                    return Audience.EMPLOYEE_ONLY;
                }
                if (normalized.endsWith("_REQUEST")) {
                    return Audience.MANAGER_ONLY;
                }
                return Audience.BOTH;
        }
    }

    /**
     * Kiểm tra mã loại thông báo từ server có được phép hiển thị cho vai trò hiện tại không.
     */
    public static boolean isAllowedForRole(@Nullable String rawType, @Nullable UserRole role) {
        if (role == null) {
            return false;
        }
        Audience audience = audienceForRawType(rawType);
        switch (audience) {
            case MANAGER_ONLY:
                return role == UserRole.MANAGER;
            case EMPLOYEE_ONLY:
                return role == UserRole.EMPLOYEE;
            case BOTH:
            default:
                return true;
        }
    }

    /**
     * Kiểm tra thông báo nhắm tới {@code targetRole} có khớp với {@code currentRole} đang đăng nhập không.
     */
    public static boolean isTargetRoleAllowed(@Nullable UserRole targetRole, @Nullable UserRole currentRole) {
        if (currentRole == null) {
            return false;
        }
        return targetRole == null || targetRole == currentRole;
    }

    /**
     * Ánh xạ mã loại thông báo từ backend sang {@link NotificationType}.
     */
    public static NotificationType mapNotificationType(@Nullable String rawType) {
        if (rawType == null) {
            return NotificationType.SYSTEM;
        }
        switch (rawType.trim().toUpperCase(Locale.US)) {
            case "CHANGE_LEAVE_REQUEST":
            case "CHANGE_LEAVE_REQUEST_RESPONSE":
            case "LEAVE_REQUEST":
            case "LEAVE_APPROVED":
            case "LEAVE":
                return NotificationType.LEAVE;

            case "ASSIGN_WORK_SCHEDULE":
            case "SCHEDULE_ASSIGNED":
            case "WORK_SCHEDULE_ASSIGNED":
            case "CHANGE_WORK_SHIFT":
            case "CHANGE_WORK_SHIFT_REQUEST":
            case "CHANGE_WORK_SHIFT_RESPONSE":
            case "SHIFT_CHANGE_REQUEST":
            case "NEW_CHANGE_REQUEST":
            case "WORK_SCHEDULE_REQUEST":
            case "SHIFT_CHANGE_APPROVED":
            case "SCHEDULE_UPDATED":
            case "SCHEDULE":
                return NotificationType.SCHEDULE;

            case "ATTENDANCE_ADJUSTMENT_REQUEST":
            case "ATTENDANCE_ADJUSTMENT_RESPONSE":
            case "ATTENDANCE_ADJUSTMENT_APPROVED":
            case "ATTENDANCE":
            case "REMINDER":
                return NotificationType.ATTENDANCE;

            case "PAYROLL":
                return NotificationType.PAYROLL;

            case "JOIN_COMPANY_REQUEST":
            case "JOIN_COMPANY_REQUEST_RESPONSE":
            default:
                return NotificationType.SYSTEM;
        }
    }
}
