package com.example.attendanceapp.data.local.pref;

import android.content.SharedPreferences;

import com.example.attendanceapp.domain.model.CheckState;
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
    private static final String KEY_COMPANY_RADIUS = "company_radius";
    private static final String KEY_COMPANY_PHONE = "company_phone";
    private static final String KEY_COMPANY_EMAIL = "company_email";
    private static final String KEY_COMPANY_TAX_CODE = "company_tax_code";
    private static final String KEY_COMPANY_WEBSITE = "company_website";
    private static final String KEY_COMPANY_INDUSTRY = "company_industry";

    // ===== LATEST SHIFT CONFIG (Lưu mốc GPS ca làm việc Quản lý vừa thiết lập) =====
    private static final String KEY_LATEST_SHIFT_LAT = "latest_shift_lat";
    private static final String KEY_LATEST_SHIFT_LON = "latest_shift_lon";
    private static final String KEY_LATEST_SHIFT_RADIUS = "latest_shift_radius";
    private static final String KEY_LATEST_SHIFT_ADDRESS = "latest_shift_address";

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

    public void saveShiftLocation(String shiftKey, double lat, double lon, double radius) {
        if (shiftKey == null) return;
        String key = shiftKey.toLowerCase().trim();
        sharedPreferences.edit()
                .putFloat("shift_lat_" + key, (float) lat)
                .putFloat("shift_lon_" + key, (float) lon)
                .putFloat("shift_radius_" + key, (float) radius)
                .apply();
    }

    public double getShiftLat(String shiftKey) {
        if (shiftKey == null) return 0.0;
        return sharedPreferences.getFloat("shift_lat_" + shiftKey.toLowerCase().trim(), 0.0f);
    }

    public double getShiftLon(String shiftKey) {
        if (shiftKey == null) return 0.0;
        return sharedPreferences.getFloat("shift_lon_" + shiftKey.toLowerCase().trim(), 0.0f);
    }

    public float getShiftRadius(String shiftKey) {
        if (shiftKey == null) return 0.0f;
        return sharedPreferences.getFloat("shift_radius_" + shiftKey.toLowerCase().trim(), 0.0f);
    }

    public void saveLatestShiftLocation(double lat, double lon, double radius, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putFloat(KEY_LATEST_SHIFT_LAT, (float) lat)
                .putFloat(KEY_LATEST_SHIFT_LON, (float) lon)
                .putFloat(KEY_LATEST_SHIFT_RADIUS, (float) radius);
        if (address != null && !address.trim().isEmpty()) {
            editor.putString(KEY_LATEST_SHIFT_ADDRESS, address);
        }
        editor.apply();
    }

    public double getLatestShiftLat() {
        return sharedPreferences.getFloat(KEY_LATEST_SHIFT_LAT, 0.0f);
    }

    public double getLatestShiftLon() {
        return sharedPreferences.getFloat(KEY_LATEST_SHIFT_LON, 0.0f);
    }

    public float getLatestShiftRadius() {
        return sharedPreferences.getFloat(KEY_LATEST_SHIFT_RADIUS, 100.0f);
    }

    public String getLatestShiftAddress() {
        return sharedPreferences.getString(KEY_LATEST_SHIFT_ADDRESS, null);
    }

    public void saveShiftAddress(String shiftKey, String address) {
        if (shiftKey == null || address == null) return;
        sharedPreferences.edit().putString("shift_addr_" + shiftKey.toLowerCase().trim(), address).apply();
    }

    public String getShiftAddress(String shiftKey) {
        if (shiftKey != null) {
            String addr = sharedPreferences.getString("shift_addr_" + shiftKey.toLowerCase().trim(), null);
            if (addr != null && !addr.trim().isEmpty()) {
                return addr;
            }
        }
        String latestAddr = getLatestShiftAddress();
        if (latestAddr != null && !latestAddr.trim().isEmpty()) {
            return latestAddr;
        }
        String compAddr = getCompanyAddress();
        if (compAddr != null && !compAddr.trim().isEmpty()) {
            return compAddr;
        }
        return null;
    }

    public String getCompanyAddress() {
        return sharedPreferences.getString(KEY_COMPANY_ADDRESS, null);
    }

    public void saveCompanyRadius(int radius) {
        sharedPreferences.edit().putInt(KEY_COMPANY_RADIUS, radius).apply();
    }

    public int getCompanyRadius() {
        return sharedPreferences.getInt(KEY_COMPANY_RADIUS, 100);
    }

    public void saveCompanyDetails(String phone, String email, String taxCode, String website, String industry) {
        sharedPreferences.edit()
                .putString(KEY_COMPANY_PHONE, phone)
                .putString(KEY_COMPANY_EMAIL, email)
                .putString(KEY_COMPANY_TAX_CODE, taxCode)
                .putString(KEY_COMPANY_WEBSITE, website)
                .putString(KEY_COMPANY_INDUSTRY, industry)
                .apply();
    }

    public String getCompanyPhone() { return sharedPreferences.getString(KEY_COMPANY_PHONE, ""); }
    public String getCompanyEmail() { return sharedPreferences.getString(KEY_COMPANY_EMAIL, ""); }
    public String getCompanyTaxCode() { return sharedPreferences.getString(KEY_COMPANY_TAX_CODE, ""); }
    public String getCompanyWebsite() { return sharedPreferences.getString(KEY_COMPANY_WEBSITE, ""); }
    public String getCompanyIndustry() { return sharedPreferences.getString(KEY_COMPANY_INDUSTRY, ""); }

    // ===== SHIFT ATTENDANCE STATE METHODS =====

    public void saveShiftAttendance(String scheduleId, String workday, String shiftName, CheckState state, String time) {
        if (state == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String stateVal = state.name();

        if (scheduleId != null && !scheduleId.trim().isEmpty()) {
            editor.putString("att_state_sched_" + scheduleId, stateVal);
            if (state == CheckState.CHECKED_IN) {
                editor.putString("att_in_sched_" + scheduleId, time);
            } else if (state == CheckState.CHECKED_OUT) {
                editor.putString("att_out_sched_" + scheduleId, time);
            }
        }

        if (workday != null && shiftName != null) {
            String alias = workday.trim() + "_" + shiftName.trim().toLowerCase();
            editor.putString("att_state_alias_" + alias, stateVal);
            if (state == CheckState.CHECKED_IN) {
                editor.putString("att_in_alias_" + alias, time);
            } else if (state == CheckState.CHECKED_OUT) {
                editor.putString("att_out_alias_" + alias, time);
            }
        }
        editor.apply();
    }

    public CheckState getShiftAttendanceState(String scheduleId, String workday, String shiftName) {
        String val = null;
        if (scheduleId != null && !scheduleId.trim().isEmpty()) {
            val = sharedPreferences.getString("att_state_sched_" + scheduleId, null);
        }
        if (val == null && workday != null && shiftName != null) {
            String alias = workday.trim() + "_" + shiftName.trim().toLowerCase();
            val = sharedPreferences.getString("att_state_alias_" + alias, null);
        }
        if (val != null) {
            try {
                return CheckState.valueOf(val);
            } catch (Exception ignored) {}
        }
        return CheckState.NOT_CHECKED_IN;
    }

    public String getShiftCheckInTime(String scheduleId, String workday, String shiftName) {
        String time = null;
        if (scheduleId != null && !scheduleId.trim().isEmpty()) {
            time = sharedPreferences.getString("att_in_sched_" + scheduleId, null);
        }
        if (time == null && workday != null && shiftName != null) {
            String alias = workday.trim() + "_" + shiftName.trim().toLowerCase();
            time = sharedPreferences.getString("att_in_alias_" + alias, null);
        }
        return time;
    }

    public String getShiftCheckOutTime(String scheduleId, String workday, String shiftName) {
        String time = null;
        if (scheduleId != null && !scheduleId.trim().isEmpty()) {
            time = sharedPreferences.getString("att_out_sched_" + scheduleId, null);
        }
        if (time == null && workday != null && shiftName != null) {
            String alias = workday.trim() + "_" + shiftName.trim().toLowerCase();
            time = sharedPreferences.getString("att_out_alias_" + alias, null);
        }
        return time;
    }

    public void saveShiftAllowedCheckInTime(String shiftKey, String time) {
        if (shiftKey == null || time == null) return;
        sharedPreferences.edit().putString("shift_allowed_in_" + shiftKey.toLowerCase().trim(), time).apply();
    }

    public String getShiftAllowedCheckInTime(String shiftKey) {
        if (shiftKey != null) {
            return sharedPreferences.getString("shift_allowed_in_" + shiftKey.toLowerCase().trim(), null);
        }
        return null;
    }

    public boolean hasShiftNotified(String shiftId, int triggerType, String workday) {
        String key = "notif_sent_" + (workday != null ? workday : "") + "_" + (shiftId != null ? shiftId : "") + "_" + triggerType;
        return sharedPreferences.getBoolean(key, false);
    }

    public void setShiftNotified(String shiftId, int triggerType, String workday, boolean notified) {
        String key = "notif_sent_" + (workday != null ? workday : "") + "_" + (shiftId != null ? shiftId : "") + "_" + triggerType;
        sharedPreferences.edit().putBoolean(key, notified).apply();
    }

    public int getMonthlyShiftExchangeCount(String yearMonth) {
        if (yearMonth == null) return 0;
        return sharedPreferences.getInt("shift_exchange_count_" + yearMonth, 0);
    }

    public void incrementMonthlyShiftExchangeCount(String yearMonth) {
        if (yearMonth == null) return;
        int current = getMonthlyShiftExchangeCount(yearMonth);
        sharedPreferences.edit().putInt("shift_exchange_count_" + yearMonth, current + 1).apply();
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