package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

public interface AppStateRepository {

    // --- 1. Trạng thái Đăng nhập & App Launch ---
    boolean isLoggedIn();
    void setLoggedIn(boolean isLoggedIn);

    boolean isAccountSaved();
    void setAccountSaved(boolean isSaved);

    boolean isFirstLaunch();
    void setFirstLaunchDone();

    // --- 2. Trạng thái Người dùng & Role (Bổ sung mới) ---
    String getUserStatus();
    void saveUserStatus(UserStatus status); // PENDING_APPROVAL, APPROVED, NEEDS_ROLE_SELECTION

    UserRole getUserRole();
    void saveUserRole(UserRole role);

    // --- 3. Token & Thông tin Tài khoản đã lưu (Bổ sung mới) ---
    String getAuthToken();
    void saveAuthToken(String token);

    SavedAccountData getSavedAccountData();
    void saveAccountData(SavedAccountData accountData);

    // --- 4. Xóa phiên làm việc khi Đăng xuất (Bổ sung mới) ---
    void clearSession();
}