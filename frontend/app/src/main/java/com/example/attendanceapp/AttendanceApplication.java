package com.example.attendanceapp;

import android.app.Application;
import android.content.SharedPreferences;

import com.example.attendanceapp.core.notification.NotificationSyncReceiver;
import com.example.attendanceapp.core.notification.SystemNotificationHelper;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AttendanceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Tạo Notification Channel hệ thống và dọn dẹp channel/notification cũ nếu có
        SystemNotificationHelper.createNotificationChannel(this);
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(9901);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    notificationManager.deleteNotificationChannel("attendance_bg_channel");
                } catch (Exception ignored) {}
            }
        }

        // 2. Buộc ứng dụng sử dụng cấu hình Dark Mode riêng từ bộ nhớ (is_dark_mode)
        SharedPreferences pref = getSharedPreferences("SHARE_PREFERENCES_APP", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("is_dark_mode", false);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                           : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );

        // 3. Khởi động chu kỳ đồng bộ thông báo nền định kỳ (AlarmManager, chạy âm thầm)
        SharedPreferences authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String accessToken = authPrefs.getString("access_token", null);
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            NotificationSyncReceiver.schedulePeriodicSync(this);
        }
    }
}