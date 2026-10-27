package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

import io.reactivex.rxjava3.core.Observable;

/**
 * Trạng thái nhẹ lưu ở SharedPreferences, đọc đồng bộ, không gọi mạng, không đụng Room.
 * Token do TokenManager quản lý. Dữ liệu hồ sơ do SessionRepository quản lý.
 */
public interface AppStateRepository {

    // --- 1. Đăng nhập & lần mở app ---
    boolean isLoggedIn();
    void setLoggedIn(boolean isLoggedIn);

    /** Nguồn duy nhất cho trạng thái đăng nhập (dùng cho Splash / tự điều hướng). */
    Observable<Boolean> observeLoggedIn();

    boolean isAccountSaved();
    void setAccountSaved(boolean isSaved);

    boolean isFirstLaunch();
    void setFirstLaunchDone();

    // --- 2. Trạng thái & Role ---
    String getUserStatus();
    void saveUserStatus(UserStatus status);

    /** Chỉ có giá trị sau khi hồ sơ theo role đã tồn tại. */
    UserRole getUserRole();
    void saveUserRole(UserRole role);

    // --- 3. Đăng xuất ---
    void clearSession();
}