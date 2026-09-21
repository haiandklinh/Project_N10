package com.example.attendanceapp;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AttendanceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Hilt đã tự động quản lý Dependency Injection thông qua annotation @HiltAndroidApp.
        // Bạn chỉ cần gọi các thư viện bên thứ 3 cần init ở đây (ví dụ: Timber, Firebase, v.v. nếu có).
    }
}