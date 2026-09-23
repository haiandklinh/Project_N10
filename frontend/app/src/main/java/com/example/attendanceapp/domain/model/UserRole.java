package com.example.attendanceapp.domain.model;

public enum UserRole {
    MANAGER("MANAGER"),
    EMPLOYEE("EMPLOYEE");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Helper method giúp convert từ String (API trả về) sang Enum
    public static UserRole fromString(String roleStr) {
        if (roleStr != null) {
            for (UserRole role : UserRole.values()) {
                if (role.value.equalsIgnoreCase(roleStr)) {
                    return role;
                }
            }
        }
        return EMPLOYEE; // Mặc định nếu không tìm thấy
    }
}