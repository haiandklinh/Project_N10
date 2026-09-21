package com.example.attendanceapp.domain.model;

import java.time.Instant;

public class UserAccount {
    private final String userId;          // UUID
    private final String username;
    private final String email;
    private final UserRole role;          // MANAGER, EMPLOYEE
    private final boolean isFirstLogin;
    private final boolean isActive;
    private final Instant lastLoginTime;

    public UserAccount(String userId, String username, String email, UserRole role,
                       boolean isFirstLogin, boolean isActive, Instant lastLoginTime) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isFirstLogin = isFirstLogin;
        this.isActive = isActive;
        this.lastLoginTime = lastLoginTime;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public boolean isFirstLogin() { return isFirstLogin; }
    public boolean isActive() { return isActive; }
    public Instant getLastLoginTime() { return lastLoginTime; }
}