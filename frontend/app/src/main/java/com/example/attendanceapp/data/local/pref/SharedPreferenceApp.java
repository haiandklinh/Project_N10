package com.example.attendanceapp.data.local.pref;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SharedPreferenceApp {

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ACCOUNT_SAVED = "is_account_saved";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_USER_ROLE = "user_role";

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
        return sharedPreferences.getString(KEY_USER_ROLE, "EMPLOYEE");
    }

    public void setUserRole(String role) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply();
    }

    /**
     * Xóa sạch trạng thái phiên làm việc khi người dùng Đăng xuất
     */
    public void clearSession() {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_ROLE)
                .apply();
    }
}