package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.data.mapper.UserMapper;
import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * AppStateRepositoryImpl - Quản lý AppState & Profile Data
 * 
 * Session State (SharedPreferences):
 * - is_logged_in, is_account_saved, is_first_launch, user_role, user_status
 * 
 * Profile Data (Room DB):
 * - user_id, full_name, phone, company_name, invitation_code
 */
@Singleton
public class AppStateRepositoryImpl implements AppStateRepository {

    private final SharedPreferenceApp sharedPreferenceApp;
    private final UserDao userDao;

    @Inject
    public AppStateRepositoryImpl(SharedPreferenceApp sharedPreferenceApp, UserDao userDao) {
        this.sharedPreferenceApp = sharedPreferenceApp;
        this.userDao = userDao;
    }

    // ===== SESSION STATE (SharedPreferences) =====

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
    public void saveUserStatus(UserStatus status) {
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
    public void clearSession() {
        sharedPreferenceApp.clearSession();
    }

    // ===== PROFILE DATA (Room DB) =====

    @Override
    public SavedAccountData getSavedAccountData() {
        try {
            UserEntity entity = userDao.getCurrentUserSingle().blockingGet();
            if (entity == null) {
                return null;
            }

            return new SavedAccountData(
                    entity.getName() != null ? entity.getName() : "Người dùng",
                    entity.getCompanyName() != null ? entity.getCompanyName() : "Công ty",
                    entity.getInvitationCode() != null ? entity.getInvitationCode() : "CODE",
                    entity.getPhone()
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveAccountData(SavedAccountData accountData) {
        if (accountData != null) {
            sharedPreferenceApp.setAccountSaved(true);
        }
    }

    // ===== TOKEN MANAGEMENT (TokenManager - EncryptedSharedPreferences) =====
    // Note: Token management should be handled by TokenManager directly
    // These methods are deprecated and should be removed in favor of TokenManager

    @Override
    public String getAuthToken() {
        // TODO: Use TokenManager instead
        return null;
    }

    @Override
    public void saveAuthToken(String token) {
        // TODO: Use TokenManager instead
    }
}