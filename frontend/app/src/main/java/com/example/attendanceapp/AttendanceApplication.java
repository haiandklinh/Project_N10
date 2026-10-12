package com.example.attendanceapp;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AttendanceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Buộc ứng dụng sử dụng cấu hình Dark Mode riêng từ bộ nhớ (is_dark_mode)
        android.content.SharedPreferences pref = getSharedPreferences("SHARE_PREFERENCES_APP", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("is_dark_mode", false);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                           : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}