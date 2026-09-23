package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;

public interface AuthRepository {
    // 1. Đăng ký chỉ cần Họ tên, SĐT, Mật khẩu (Chưa có role)
    void register(String fullName, String phoneNumber, String password, RegisterCallback callback);

    // 2. Đăng nhập
    void login(String phoneNumber, String password, LoginCallback callback);

    // 3. Hàm chọn/cập nhật vai trò sau khi đăng nhập thành công
    void selectRole(UserRole role, RoleCallback callback);

    // 4. Các hàm xử lý công ty theo vai trò
    void createCompany(String companyName, CompanyCallback callback);
    void joinCompanyWithCode(String invitationCode, CompanyCallback callback);

    // 5. OTP & Quên mật khẩu
    void sendOtp(String phoneNumber, OtpCallback callback);
    void verifyOtp(String phoneNumber, String otp, OtpCallback callback);
    void resetPassword(String phoneNumber, String newPassword, ResetPasswordCallback callback);
    void logout();

    // --- Callbacks ---
    interface RegisterCallback {
        void onSuccess(UserAccount user);
        void onError(String error);
    }

    interface LoginCallback {
        void onSuccess(UserAccount user);
        void onError(String error);
    }

    interface RoleCallback {
        void onSuccess();
        void onError(String error);
    }

    interface CompanyCallback {
        void onSuccess(String companyName);
        void onError(String error);
    }

    interface OtpCallback {
        void onSuccess();
        void onError(String error);
    }

    interface ResetPasswordCallback {
        void onSuccess();
        void onError(String error);
    }
}