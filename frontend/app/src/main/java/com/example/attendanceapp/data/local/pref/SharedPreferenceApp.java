package com.example.attendanceapp.data.local.pref;

import android.content.SharedPreferences;

import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * SharedPreferenceApp - Chỉ lưu trữ Session State & Cấu hình ứng dụng
 * 
 * NHÓM 1: Session/Auth State (Key-Value truy cập nhanh cho Splash Screen routing)
 * - auth_token (BẮT BUỘC - dùng TokenManager với EncryptedSharedPreferences để bảo mật)
 * - is_logged_in
 * - is_account_saved
 * - is_first_launch
 * - user_id (ID người dùng hiện tại đang active)
 * - user_role (Routing nhanh giao diện Manager hay Employee khi mở App)
 * - user_status
 * 
 * NHÓM 2: Profile Data -> KHÔNG LƯU Ở ĐÂY
 * - full_name, gender, date_of_birth, avatar_url, phone -> Lưu vào Room DB (UserEntity)
 */
@Singleton
public class SharedPreferenceApp {

    // ===== SESSION STATE =====
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ACCOUNT_SAVED = "is_account_saved";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_STATUS = "user_status";
    private static final String KEY_USER_ID = "user_id";

    // ===== COMPANY CONFIG =====
    private static final String KEY_COMPANY_LAT = "company_lat";
    private static final String KEY_COMPANY_LON = "company_lon";
    private static final String KEY_COMPANY_ADDRESS = "company_address";

    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferenceApp(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    // ===== SESSION STATE METHODS =====

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

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void setUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public void saveUserStatus(UserStatus status) {
        sharedPreferences.edit()
                .putString(KEY_USER_STATUS, status.name())
                .apply();
    }

    public String getUserStatus() {
        return sharedPreferences.getString(KEY_USER_STATUS, null);
    }

    // ===== COMPANY CONFIG METHODS =====

    public void saveCompanyLocation(double lat, double lon, String address) {
        sharedPreferences.edit()
                .putFloat(KEY_COMPANY_LAT, (float) lat)
                .putFloat(KEY_COMPANY_LON, (float) lon)
                .putString(KEY_COMPANY_ADDRESS, address)
                .apply();
    }

    public double getCompanyLat() {
        return sharedPreferences.getFloat(KEY_COMPANY_LAT, 0.0f);
    }

    public double getCompanyLon() {
        return sharedPreferences.getFloat(KEY_COMPANY_LON, 0.0f);
    }

    public String getCompanyAddress() {
        return sharedPreferences.getString(KEY_COMPANY_ADDRESS, null);
    }

    /**
     * Xóa sạch trạng thái phiên làm việc khi người dùng Đăng xuất
     * KHÔNG xóa KEY_IS_ACCOUNT_SAVED để giữ tài khoản đã lưu cho lần đăng nhập sau
     */
    public void clearSession() {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_STATUS)
                .remove(KEY_USER_ID)
                .apply();
    }
}