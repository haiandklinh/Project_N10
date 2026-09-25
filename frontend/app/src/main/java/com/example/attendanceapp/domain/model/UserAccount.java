package com.example.attendanceapp.domain.model;

import java.time.Instant;
import java.time.LocalDate;

public class UserAccount {
    private final String userId;          // UUID
    private final String fullName;              // Thay đổi từ username -> fullName (có getter/setter)
    private final String phoneNumber;
    private final String gender;          // Giới tính (Ví dụ: "Nam", "Nữ", "Khác")
    private final LocalDate dateOfBirth;  // Ngày sinh (Sử dụng LocalDate cho kiểu ngày tháng)
    private final String avatarUrl;             // Đường dẫn/URL ảnh đại diện
    private final UserRole role;          // MANAGER, EMPLOYEE
    private final boolean isFirstLogin;
    private final boolean isActive;
    private final Instant lastLoginTime;
    private final UserStatus status;     // APPROVED, PENDING_APPROVAL, NEEDS_ROLE_SELECTION

    public UserAccount(String userId, String fullName, String phoneNumber, String gender,
                       LocalDate dateOfBirth, String avatarUrl, UserRole role,
                       boolean isFirstLogin, boolean isActive, Instant lastLoginTime, UserStatus status) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isFirstLogin = isFirstLogin;
        this.isActive = isActive;
        this.lastLoginTime = lastLoginTime;
        this.status = status;
    }

    // Getters & Setters
    public String getUserId() { return userId; }

    public String getFullName() { return fullName; }

    public String getPhoneNumber() { return phoneNumber; }

    public String getGender() { return gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public String getAvatarUrl() { return avatarUrl; }

    public UserRole getRole() { return role; }

    public boolean isFirstLogin() { return isFirstLogin; }

    public boolean isActive() { return isActive; }

    public Instant getLastLoginTime() { return lastLoginTime; }

    public UserStatus getStatus() { return status; }

    // Thay setter bằng method trả về BẢN MỚI, giữ nguyên các field khác
    public UserAccount withPersonalInfo(String fullName, LocalDate dateOfBirth, String gender) {
        return new UserAccount(userId, fullName, phoneNumber, gender, dateOfBirth, avatarUrl,
                role, isFirstLogin, isActive, lastLoginTime, status);
    }

    public UserAccount withRole(UserRole role) {
        return new UserAccount(userId, fullName, phoneNumber, gender, dateOfBirth, avatarUrl,
                role, isFirstLogin, isActive, lastLoginTime, status);
    }

    public UserAccount withStatus(UserStatus status) {
        return new UserAccount(userId, fullName, phoneNumber, gender, dateOfBirth, avatarUrl,
                role, isFirstLogin, isActive, lastLoginTime, status);
    }

}