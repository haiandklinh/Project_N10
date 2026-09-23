package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppStateRepositoryImpl implements AppStateRepository {

    private final SharedPreferenceApp sharedPreferenceApp;

    @Inject
    public AppStateRepositoryImpl(SharedPreferenceApp sharedPreferenceApp) {
        this.sharedPreferenceApp = sharedPreferenceApp;
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferenceApp.isLoggedIn();
    }

    @Override
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferenceApp.setLoggedIn(isLoggedIn);
    }

    @Override
    public boolean isAccountSaved() {
        return sharedPreferenceApp.isAccountSaved();
    }

    @Override
    public void setAccountSaved(boolean isSaved) {
        sharedPreferenceApp.setAccountSaved(isSaved);
    }

    @Override
    public boolean isFirstLaunch() {
        return sharedPreferenceApp.isFirstLaunch();
    }

    @Override
    public void setFirstLaunchDone() {
        sharedPreferenceApp.setFirstLaunchDone();
    }

    @Override
    public String getUserStatus() {
        return sharedPreferenceApp.getUserStatus();
    }

    @Override
    public void saveUserStatus(String status) {
        sharedPreferenceApp.saveUserStatus(status);
    }

    @Override
    public UserRole getUserRole() {
        String roleStr = sharedPreferenceApp.getUserRole();
        return UserRole.fromString(roleStr);
    }

    @Override
    public void saveUserRole(UserRole role) {
        if (role != null) {
            sharedPreferenceApp.setUserRole(role.getValue());
        }
    }

    @Override
    public String getAuthToken() {
        return sharedPreferenceApp.getAuthToken();
    }

    @Override
    public void saveAuthToken(String token) {
        sharedPreferenceApp.saveAuthToken(token);
    }

    @Override
    public SavedAccountData getSavedAccountData() {
        String userId = sharedPreferenceApp.getUserId();
        String username = sharedPreferenceApp.getUsername();
        String phone = sharedPreferenceApp.getPhone();

        if (userId == null || phone == null) {
            return null;
        }

        // Đọc dữ liệu tài khoản đã lưu từ SharedPreferences
        return new SavedAccountData(
                username != null ? username : "Người dùng",
                "Công ty", // TODO: Lưu tên công ty vào SharedPreferences khi tạo công ty
                "CODE", // TODO: Lưu mã công ty vào SharedPreferences khi tham gia công ty
                phone
        );
    }

    @Override
    public void saveAccountData(SavedAccountData accountData) {
        if (accountData != null) {
            sharedPreferenceApp.setAccountSaved(true);
        }
    }

    @Override
    public void clearSession() {
        sharedPreferenceApp.clearSession();
    }
}