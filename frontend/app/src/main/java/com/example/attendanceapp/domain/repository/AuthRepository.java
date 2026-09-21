package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserAccount;

public interface AuthRepository {
    void register(String fullName, String companyName, String role, String contact, boolean isEmail, String password, RegisterCallback callback);
    void login(String username, String password, LoginCallback callback);
    void sendOtp(String account, OtpCallback callback);
    void verifyOtp(String account, String otp, OtpCallback callback);
    void resetPassword(String account, String newPassword, ResetPasswordCallback callback);
    void logout();
    interface RegisterCallback {
        void onSuccess(UserAccount user);
        void onError(String error);
    }

    interface LoginCallback {
        void onSuccess(UserAccount user);
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
