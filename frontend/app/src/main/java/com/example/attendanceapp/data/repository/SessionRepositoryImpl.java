package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionRepositoryImpl implements SessionRepository {

    private final SharedPreferenceApp sharedPreferenceApp;

    @Inject
    public SessionRepositoryImpl(SharedPreferenceApp sharedPreferenceApp) {
        this.sharedPreferenceApp = sharedPreferenceApp;
    }

    // =========================================================
    // SESSION & PREFERENCES MANAGEMENT
    // =========================================================

    @Override
    public UserRole getSavedUserRole() {
        String roleStr = sharedPreferenceApp.getUserRole();
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            return UserRole.EMPLOYEE;
        }
    }

    @Override
    public void saveUserRole(UserRole role) {
        if (role == null) return;
        sharedPreferenceApp.setUserRole(role.name());
    }

    @Override
    public UserAccount getCurrentUser() {
        if (!isLoggedIn()) return null;
        return sharedPreferenceApp.getUserInfo();
    }

    @Override
    public void saveUserSession(UserAccount userAccount, boolean isLoggedIn) {
        if (userAccount == null) return;
        
        sharedPreferenceApp.setLoggedIn(isLoggedIn);
        sharedPreferenceApp.saveUserInfo(userAccount);
        sharedPreferenceApp.savePhone(userAccount.getPhoneNumber());
        if (userAccount.getRole() != null) {
            sharedPreferenceApp.setUserRole(userAccount.getRole().name());
        }
    }

    @Override
    public void saveUserStatus(UserStatus status) {
        sharedPreferenceApp.saveUserStatus(status);
    }

    @Override
    public String getUserStatus() {
        return sharedPreferenceApp.getUserStatus();
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferenceApp.isLoggedIn();
    }

    @Override
    public void clearUserSession() {
        sharedPreferenceApp.clearSession();
    }
}