package com.example.attendanceapp.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model chính phục vụ cho UI và quản lý phiên (Session).
 * Bao gồm các thông tin kỹ thuật của tài khoản (phone, status) và thông tin hồ sơ người dùng.
 * Model này bất biến (Immutable).
 */
public final class UserAccount {
    
    private final String phoneNumber;
    private final boolean firstLogin;
    private final boolean active;
    @Nullable private final Instant lastLoginTime;
    
    // Thông tin bổ sung phục vụ logic UI
    @Nullable private final String invitationCode; // Chỉ dành cho Manager
    @Nullable private final String managerId;      // Chỉ dành cho Staff

    // Hồ sơ người dùng (Khớp với dữ liệu từ ProfileResponse/MeResponse)
    private final UserProfile profile;

    private UserAccount(Builder b) {
        this.phoneNumber = Objects.requireNonNull(b.phoneNumber, "phoneNumber cannot be null");
        this.firstLogin = b.firstLogin;
        this.active = b.active;
        this.lastLoginTime = b.lastLoginTime;
        this.invitationCode = b.invitationCode;
        this.managerId = b.managerId;
        this.profile = Objects.requireNonNull(b.profile, "profile cannot be null");
    }

    // ===== Getters phục vụ UI (Flat Getters để thuận tiện truy xuất) =====
    public String getUserId() { return profile.getUserId(); }
    @Nullable public String getFullName() { return profile.getFullName(); }
    public String getPhoneNumber() { return phoneNumber; }
    @Nullable public Gender getGender() { return profile.getGender(); }
    @Nullable public LocalDate getDateOfBirth() { return profile.getDateOfBirth(); }
    @Nullable public String getAvatarUrl() { return profile.getAvatarUrl(); }
    @Nullable public UserRole getRole() { return profile.getRole(); }
    public UserStatus getStatus() { return profile.getStatus(); }
    
    @Nullable public String getCompanyName() { return profile.getCompanyName(); }
    @Nullable public String getCompanyId() { return profile.getCompanyId(); }
    
    public boolean isFirstLogin() { return firstLogin; }
    public boolean isActive() { return active; }
    @Nullable public Instant getLastLoginTime() { return lastLoginTime; }
    @Nullable public String getInvitationCode() { return invitationCode; }
    @Nullable public String getManagerId() { return managerId; }

    /** Trả về đối tượng profile gốc */
    public UserProfile getProfile() { return profile; }

    // ===== Helper methods for UI =====
    public boolean isManager() { return profile.getRole() == UserRole.MANAGER; }
    public boolean isEmployee() { return profile.getRole() == UserRole.EMPLOYEE; }
    
    public String getDisplayName() {
        return (profile.getFullName() != null && !profile.getFullName().isEmpty()) 
                ? profile.getFullName() 
                : phoneNumber;
    }

    // ===== Immutable Updates (Trả về bản copy mới) =====
    public UserAccount withProfile(UserProfile newProfile) {
        return toBuilder().profile(newProfile).build();
    }
    
    public UserAccount withPersonalInfo(String fullName, LocalDate dob, Gender gender) {
        UserProfile newProfile = profile.toBuilder()
                .fullName(fullName)
                .dateOfBirth(dob)
                .gender(gender)
                .build();
        return withProfile(newProfile);
    }

    public UserAccount withStatus(UserStatus status) {
        return withProfile(profile.toBuilder().status(status).build());
    }

    public UserAccount withRole(UserRole role) {
        return withProfile(profile.toBuilder().role(role).build());
    }

    public Builder toBuilder() { return new Builder(this); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount)) return false;
        UserAccount that = (UserAccount) o;
        return firstLogin == that.firstLogin &&
                active == that.active &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(lastLoginTime, that.lastLoginTime) &&
                Objects.equals(invitationCode, that.invitationCode) &&
                Objects.equals(managerId, that.managerId) &&
                Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, firstLogin, active, lastLoginTime, invitationCode, managerId, profile);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserAccount{userId=" + getUserId() + ", role=" + getRole() + ", status=" + getStatus() + "}";
    }

    // ===== Builder =====
    public static final class Builder {
        private String phoneNumber, invitationCode, managerId;
        private boolean firstLogin = true;
        private boolean active = true;
        private Instant lastLoginTime;
        private UserProfile profile;

        public Builder() { }

        private Builder(UserAccount a) {
            this.phoneNumber = a.phoneNumber;
            this.firstLogin = a.firstLogin;
            this.active = a.active;
            this.lastLoginTime = a.lastLoginTime;
            this.invitationCode = a.invitationCode;
            this.managerId = a.managerId;
            this.profile = a.profile;
        }

        public Builder phoneNumber(String v) { phoneNumber = v; return this; }
        public Builder firstLogin(boolean v) { firstLogin = v; return this; }
        public Builder active(boolean v) { active = v; return this; }
        public Builder lastLoginTime(@Nullable Instant v) { lastLoginTime = v; return this; }
        public Builder invitationCode(@Nullable String v) { invitationCode = v; return this; }
        public Builder managerId(@Nullable String v) { managerId = v; return this; }
        public Builder profile(UserProfile v) { profile = v; return this; }

        public UserAccount build() { return new UserAccount(this); }
    }
}
