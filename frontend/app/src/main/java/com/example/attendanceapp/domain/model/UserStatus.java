package com.example.attendanceapp.domain.model;

public enum UserStatus {
    NEEDS_ROLE_SELECTION,
    PENDING_APPROVAL,
    APPROVED,
    ACTIVE,
    UNKNOWN; // fallback an toàn khi backend trả giá trị không xác định

    public static UserStatus fromString(String statusStr) {
        if (statusStr == null) {
            return UNKNOWN;
        }
        for (UserStatus status : UserStatus.values()) {
            if (status.name().equalsIgnoreCase(statusStr)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}