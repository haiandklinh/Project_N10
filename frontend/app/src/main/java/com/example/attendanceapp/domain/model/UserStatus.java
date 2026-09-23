package com.example.attendanceapp.domain.model;

public enum UserStatus {
    APPROVED("APPROVED"),
    PENDING_APPROVAL("PENDING_APPROVAL"),
    NEEDS_ROLE_SELECTION("NEEDS_ROLE_SELECTION");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserStatus fromString(String statusStr) {
        if (statusStr != null) {
            for (UserStatus status : UserStatus.values()) {
                if (status.value.equalsIgnoreCase(statusStr)) {
                    return status;
                }
            }
        }
        return NEEDS_ROLE_SELECTION; // Mặc định nếu không tìm thấy
    }
}
