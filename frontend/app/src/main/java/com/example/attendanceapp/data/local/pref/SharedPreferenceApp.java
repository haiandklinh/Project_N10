package com.example.attendanceapp.data.local.pref;

import android.content.SharedPreferences;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SharedPreferenceApp {

    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ACCOUNT_SAVED = "is_account_saved";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_STATUS = "user_status";
    private static final String KEY_USER_ID = "user_id";

    // Đã đổi KEY_USERNAME -> KEY_FULL_NAME và bổ sung các trường profile mới
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private static final String KEY_PHONE = "phone";
    private static final String KEY_PASSWORD = "password";

    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferenceApp(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isAccountSaved() {
        return sharedPreferences.getBoolean(KEY_IS_ACCOUNT_SAVED, false);
    }

    public void setAccountSaved(boolean isSaved) {
        sharedPreferences.edit().putBoolean(KEY_IS_ACCOUNT_SAVED, isSaved).apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    public void setUserRole(String role) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply();
    }

    // Login - Register - ForgotPW
    public void saveAuthToken(String token) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveUserStatus(UserStatus status) {
        sharedPreferences.edit()
                .putString(KEY_USER_STATUS, status.name())  // .name() -> "APPROVED", "PENDING_APPROVAL"...
                .apply();
    }

    public String getUserStatus() {
        return sharedPreferences.getString(KEY_USER_STATUS, null);
    }

    // --- LƯU VÀ LẤY THÔNG TIN USER ACCOUNT ---

    public void saveUserInfo(UserAccount user) {
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString(KEY_USER_ID, user.getUserId())
                .putString(KEY_FULL_NAME, user.getFullName())
                .putString(KEY_GENDER, user.getGender())
                .putString(KEY_AVATAR_URL, user.getAvatarUrl());

        if (user.getPhoneNumber() != null) {
            editor.putString(KEY_PHONE, user.getPhoneNumber());
        }

        if (user.getDateOfBirth() != null) {
            editor.putString(KEY_DATE_OF_BIRTH, user.getDateOfBirth().toString()); // ISO-8601 string (yyyy-MM-dd)
        } else {
            editor.remove(KEY_DATE_OF_BIRTH);
        }

        if (user.getRole() != null) {
            editor.putString(KEY_USER_ROLE, user.getRole().name());
        }

        if (user.getStatus() != null) {
            editor.putString(KEY_USER_STATUS, user.getStatus().name());
        }

        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void saveFullName(String fullName) {
        sharedPreferences.edit().putString(KEY_FULL_NAME, fullName).apply();
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
    }

    public String getGender() {
        return sharedPreferences.getString(KEY_GENDER, null);
    }

    public String getAvatarUrl() {
        return sharedPreferences.getString(KEY_AVATAR_URL, null);
    }

    public LocalDate getDateOfBirth() {
        String dobStr = sharedPreferences.getString(KEY_DATE_OF_BIRTH, null);
        if (dobStr != null && !dobStr.isEmpty()) {
            try {
                return LocalDate.parse(dobStr);
            } catch (Exception ignored) {}
        }
        return null;
    }

    public UserAccount getUserInfo() {
        String userId = getUserId();
        String fullName = getFullName();
        String phone = getPhone();
        String gender = getGender();
        LocalDate dob = getDateOfBirth();
        String avatarUrl = getAvatarUrl();
        String roleStr = getUserRole();
        String statusStr = getUserStatus();

        if (userId == null && fullName == null && phone == null) {
            return null;
        }

        UserRole role = UserRole.EMPLOYEE;
        try {
            if (roleStr != null) {
                role = UserRole.valueOf(roleStr);
            }
        } catch (Exception ignored) {}

        UserStatus status = UserStatus.NEEDS_ROLE_SELECTION;
        try {
            if (statusStr != null) {
                status = UserStatus.valueOf(statusStr);
            }
        } catch (Exception ignored) {}

        return new UserAccount(
                userId != null ? userId : "",
                fullName != null ? fullName : "",
                phone != null ? phone : "",
                gender != null ? gender : "",
                dob,
                avatarUrl,
                role,
                false,
                true,
                null,
                status
        );
    }

    public void savePhone(String phone) {
        sharedPreferences.edit().putString(KEY_PHONE, phone).apply();
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public void savePassword(String password) {
        sharedPreferences.edit().putString(KEY_PASSWORD, password).apply();
    }

    public String getPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, null);
    }

    /**
     * Xóa sạch trạng thái phiên làm việc khi người dùng Đăng xuất
     * KHÔNG xóa KEY_IS_ACCOUNT_SAVED để giữ tài khoản đã lưu cho lần đăng nhập sau
     * KHÔNG xóa thông tin tài khoản (userId, fullName, phone, password,...) để hiển thị trên LoginSavedAccountFragment
     */
    public void clearSession() {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_ROLE)
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_USER_STATUS)
                .apply();
    }
}