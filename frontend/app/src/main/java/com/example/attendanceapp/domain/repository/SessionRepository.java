package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

/**
 * Chỉ chịu trách nhiệm về SESSION & LOCAL PREFERENCES.
 * Không gọi Database/API — mọi thao tác remote nằm ở AuthRepository.
 */
public interface SessionRepository {

    /** Lấy Vai trò (Role) của người dùng đang đăng nhập đã được lưu trong bộ nhớ */
    UserRole getSavedUserRole();

    /** Lưu thông tin Vai trò người dùng vào bộ nhớ cục bộ */
    void saveUserRole(UserRole role);

    /** Lấy thông tin chi tiết tài khoản đang lưu phiên đăng nhập */
    UserAccount getCurrentUser();

    /** Lưu phiên đăng nhập người dùng */
    void saveUserSession(UserAccount userAccount, boolean isLoggedIn);

    /** Lưu trạng thái duyệt của người dùng (APPROVED, PENDING_APPROVAL, NEEDS_ROLE_SELECTION) */
    void saveUserStatus(UserStatus status);

    /** Lấy trạng thái duyệt của người dùng */
    String getUserStatus();

    /** Kiểm tra trạng thái người dùng đã đăng nhập hay chưa */
    boolean isLoggedIn();

    /** Đăng xuất cục bộ - Xóa sạch thông tin phiên làm việc đã lưu */
    void clearUserSession();
}