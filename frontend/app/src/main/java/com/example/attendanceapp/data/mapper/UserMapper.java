package com.example.attendanceapp.data.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.data.dto.response.profile.ProfileResponse;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserProfile;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

import java.time.LocalDate;

public final class UserMapper {

    private UserMapper() { }

    /** Map trực tiếp từ ProfileResponse sang UserProfile domain */
    @NonNull
    public static UserProfile toUserProfile(@Nullable ProfileResponse response) {
        if (response == null) {
            return new UserProfile.Builder().status(UserStatus.NEEDS_ROLE_SELECTION).build();
        }

        return new UserProfile.Builder()
                .fullName(response.getFullName())
                .gender(parseGender(response.getGender()))
                .dateOfBirth(parseDate(response.getDate()))
                .avatarUrl(response.getAvatarUrl())
                .status(parseStatus(response.getStatus()))
                .build();
    }

    /** Map từ ProfileResponse phẳng (kết quả GET /profile) sang UserAccount domain */
    @NonNull
    public static UserAccount toDomain(@NonNull ProfileResponse response, @NonNull String phoneNumber, @Nullable String role) {
        UserProfile profile = new UserProfile.Builder()
                .userId(phoneNumber) // Giả định dùng số điện thoại làm ID tạm nếu BE không trả về user_id
                .fullName(response.getFullName())
                .gender(parseGender(response.getGender()))
                .dateOfBirth(parseDate(response.getDate()))
                .avatarUrl(response.getAvatarUrl())
                .role(parseRole(role))
                .status(parseStatus(response.getStatus()))
                .build();

        boolean isFirstLogin = role == null || profile.getStatus() == UserStatus.NEEDS_ROLE_SELECTION;

        return new UserAccount.Builder()
                .phoneNumber(phoneNumber)
                .firstLogin(isFirstLogin)
                .profile(profile)
                .build();
    }

    /** Map từ UserAccount sang UserEntity để lưu vào Room Database */
    @NonNull
    public static UserEntity toEntity(@NonNull UserAccount userAccount) {
        UserProfile profile = userAccount.getProfile();

        return new UserEntity(
                profile.getUserId(),
                profile.getFullName(),
                userAccount.getPhoneNumber(),
                profile.getRole() != null ? profile.getRole().name() : null,
                profile.getStatus().name(),
                profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null,
                profile.getGender() != null ? profile.getGender().name() : null,
                profile.getAvatarUrl(),
                profile.getCompanyId(),
                profile.getCompanyName(),
                userAccount.getInvitationCode(),
                userAccount.isFirstLogin(),
                userAccount.isActive()
        );
    }

    /** Map từ UserEntity sang UserAccount để đọc từ Room Database */
    @NonNull
    public static UserAccount fromEntity(@NonNull UserEntity entity) {
        UserProfile profile = new UserProfile.Builder()
                .userId(entity.getUserId() != null ? entity.getUserId() : "")
                .fullName(entity.getName())
                .gender(parseGender(entity.getGender()))
                .dateOfBirth(parseDate(entity.getDateOfBirth()))
                .avatarUrl(entity.getAvatarUrl())
                .role(parseRole(entity.getRole()))
                .status(parseStatus(entity.getStatus()))
                .companyId(entity.getCompanyId())
                .companyName(entity.getCompanyName())
                .build();

        return new UserAccount.Builder()
                .phoneNumber(entity.getPhone())
                .invitationCode(entity.getInvitationCode())
                .firstLogin(entity.isFirstLogin())
                .active(entity.isActive())
                .profile(profile)
                .build();
    }

    private static UserStatus parseStatus(@Nullable String s) {
        if (s == null) return UserStatus.NEEDS_ROLE_SELECTION;
        switch (s.toUpperCase()) {
            case "ACTIVE":
            case "APPROVED":
                return UserStatus.APPROVED;
            case "PENDING_APPROVAL":
                return UserStatus.PENDING_APPROVAL;
            default:
                return UserStatus.NEEDS_ROLE_SELECTION;
        }
    }

    @Nullable
    private static UserRole parseRole(@Nullable String s) {
        if (s == null) return null;
        try {
            return UserRole.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    private static Gender parseGender(@Nullable String s) {
        if (s == null) return null;
        try {
            return Gender.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    private static LocalDate parseDate(@Nullable String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s); // yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }
}