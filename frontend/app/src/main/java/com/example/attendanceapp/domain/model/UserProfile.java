package com.example.attendanceapp.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model bất biến đại diện cho hồ sơ người dùng, khớp với ProfileResponse từ API.
 * Chứa các thông tin cá nhân và thông tin liên quan đến công ty/vai trò.
 */
public final class UserProfile {

    private final String userId;
    @Nullable private final String fullName;
    @Nullable private final LocalDate dateOfBirth;
    @Nullable private final Gender gender;
    @Nullable private final String avatarUrl;
    @Nullable private final UserRole role;
    private final UserStatus status;
    @Nullable private final String companyName;
    @Nullable private final String companyId;

    private UserProfile(Builder b) {
        this.userId = b.userId;
        this.fullName = b.fullName;
        this.dateOfBirth = b.dateOfBirth;
        this.gender = b.gender;
        this.avatarUrl = b.avatarUrl;
        this.role = b.role;
        this.status = b.status != null ? b.status : UserStatus.NEEDS_ROLE_SELECTION;
        this.companyName = b.companyName;
        this.companyId = b.companyId;
    }

    // ===== Getters =====
    public String getUserId() { return userId; }
    @Nullable public String getFullName() { return fullName; }
    @Nullable public LocalDate getDateOfBirth() { return dateOfBirth; }
    @Nullable public Gender getGender() { return gender; }
    @Nullable public String getAvatarUrl() { return avatarUrl; }
    @Nullable public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    @Nullable public String getCompanyName() { return companyName; }
    @Nullable public String getCompanyId() { return companyId; }

    public Builder toBuilder() { return new Builder(this); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(fullName, that.fullName) &&
                Objects.equals(dateOfBirth, that.dateOfBirth) &&
                gender == that.gender &&
                Objects.equals(avatarUrl, that.avatarUrl) &&
                role == that.role &&
                status == that.status &&
                Objects.equals(companyName, that.companyName) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fullName, dateOfBirth, gender, avatarUrl, role, status, companyName, companyId);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserProfile{userId='" + userId + "', fullName='" + fullName + "', role=" + role + "}";
    }

    // ===== Builder =====
    public static final class Builder {
        private String userId, fullName, avatarUrl, companyName, companyId;
        private LocalDate dateOfBirth;
        private Gender gender;
        private UserRole role;
        private UserStatus status;

        public Builder() { }

        private Builder(UserProfile p) {
            this.userId = p.userId;
            this.fullName = p.fullName;
            this.dateOfBirth = p.dateOfBirth;
            this.gender = p.gender;
            this.avatarUrl = p.avatarUrl;
            this.role = p.role;
            this.status = p.status;
            this.companyName = p.companyName;
            this.companyId = p.companyId;
        }

        public Builder userId(String v) { userId = v; return this; }
        public Builder fullName(@Nullable String v) { fullName = v; return this; }
        public Builder dateOfBirth(@Nullable LocalDate v) { dateOfBirth = v; return this; }
        public Builder gender(@Nullable Gender v) { gender = v; return this; }
        public Builder avatarUrl(@Nullable String v) { avatarUrl = v; return this; }
        public Builder role(@Nullable UserRole v) { role = v; return this; }
        public Builder status(UserStatus v) { status = v; return this; }
        public Builder companyName(@Nullable String v) { companyName = v; return this; }
        public Builder companyId(@Nullable String v) { companyId = v; return this; }

        public UserProfile build() { return new UserProfile(this); }
    }
}
