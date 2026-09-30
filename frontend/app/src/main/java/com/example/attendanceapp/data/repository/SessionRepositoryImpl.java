package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.data.mapper.UserMapper;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;

/**
 * SessionRepositoryImpl - Quản lý Session State & Profile Data
 * 
 * Session State (SharedPreferences):
 * - is_logged_in, user_role, user_status, user_id
 * 
 * Profile Data (Room DB):
 * - full_name, gender, date_of_birth, avatar_url, phone
 */
@Singleton
public class SessionRepositoryImpl implements SessionRepository {

    private final SharedPreferenceApp sharedPreferenceApp;
    private final UserDao userDao;

    @Inject
    public SessionRepositoryImpl(SharedPreferenceApp sharedPreferenceApp, UserDao userDao) {
        this.sharedPreferenceApp = sharedPreferenceApp;
        this.userDao = userDao;
    }

    // =========================================================
    // SESSION STATE (SharedPreferences)
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
        userDao.clearAllUsers();
    }

    // =========================================================
    // PROFILE DATA (Room DB)
    // =========================================================

    @Override
    public UserAccount getCurrentUser() {
        if (!isLoggedIn()) return null;
        try {
            UserEntity entity = userDao.getCurrentUserSingle().blockingGet();
            return UserMapper.fromEntity(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveUserSession(UserAccount userAccount, boolean isLoggedIn) {
        if (userAccount == null) return;
        
        // Lưu session state vào SharedPreferences
        sharedPreferenceApp.setLoggedIn(isLoggedIn);
        sharedPreferenceApp.setUserId(userAccount.getProfile().getUserId());
        if (userAccount.getRole() != null) {
            sharedPreferenceApp.setUserRole(userAccount.getRole().name());
        }
        if (userAccount.getStatus() != null) {
            sharedPreferenceApp.saveUserStatus(userAccount.getStatus());
        }
        
        // Lưu profile data vào Room DB
        UserEntity entity = UserMapper.toEntity(userAccount);
        userDao.insertOrUpdateUser(entity);
    }
}