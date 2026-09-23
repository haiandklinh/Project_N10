package com.example.attendanceapp.data.repository;

import android.content.SharedPreferences;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {

    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phoneNumber";

    private final SharedPreferences sharedPreferences;

    @Inject
    public UserRepositoryImpl(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public UserRole getSavedUserRole() {
        String roleStr = sharedPreferences.getString(KEY_USER_ROLE, UserRole.EMPLOYEE.name());
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            return UserRole.EMPLOYEE; // Mặc định trả về EMPLOYEE nếu lỗi
        }
    }

    @Override
    public void saveUserRole(UserRole role) {
        if (role == null) return;
        sharedPreferences.edit()
                .putString(KEY_USER_ROLE, role.name())
                .apply();
    }

    @Override
    public UserAccount getCurrentUser() {
        if (!isLoggedIn()) return null;

        String userId = sharedPreferences.getString(KEY_USER_ID, "");
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String phoneNumber = sharedPreferences.getString(KEY_PHONE, "");
        UserRole role = getSavedUserRole();

        return new UserAccount(userId, username, phoneNumber, role, false, true, null);
    }

    @Override
    public void saveUserSession(UserAccount userAccount, boolean isLoggedIn) {
        if (userAccount == null) return;

        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(KEY_USER_ID, userAccount.getUserId())
                .putString(KEY_USERNAME, userAccount.getUsername())
                .putString(KEY_PHONE, userAccount.getPhoneNumber())
                .putString(KEY_USER_ROLE, userAccount.getRole().name())
                .apply();
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    @Override
    public void clearUserSession() {
        sharedPreferences.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_PHONE)
                .apply();
    }
}