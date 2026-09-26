package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;

public interface UserRepository {

    /**
     * Lấy Vai trò (Role) của người dùng đang đăng nhập đã được lưu trong bộ nhớ
     */
    UserRole getSavedUserRole();

    /**
     * Lưu thông tin Vai trò người dùng vào bộ nhớ cục bộ
     */
    void saveUserRole(UserRole role);

    /**
     * Lấy thông tin chi tiết tài khoản đang lưu phiên đăng nhập
     */
    UserAccount getCurrentUser();

    /**
     * Lưu phiên đăng nhập người dùng
     */
    void saveUserSession(UserAccount userAccount, boolean isLoggedIn);

    /**
     * Kiểm tra trạng thái người dùng đã đăng nhập hay chưa
     */
    boolean isLoggedIn();

    /**
     * Đăng xuất - Xóa sạch thông tin phiên làm việc
     */
    void clearUserSession();
}