package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final SharedPreferenceApp sharedPreferenceApp;

    @Inject
    public AuthRepositoryImpl(SharedPreferenceApp sharedPreferenceApp) {
        this.sharedPreferenceApp = sharedPreferenceApp;
    }

    @Override
    public void register(String fullName, String companyName, String role, String contact, boolean isEmail, String password, RegisterCallback callback) {
        // TODO: Implement đăng ký
    }

    @Override
    public void login(String username, String password, LoginCallback callback) {
        // TODO: Implement đăng nhập
        // Sau khi đăng nhập thành công, nhớ lưu session:
        // sharedPreferenceApp.setLoggedIn(true);
    }

    @Override
    public void sendOtp(String account, OtpCallback callback) {
        // TODO: Implement gửi OTP
    }

    @Override
    public void verifyOtp(String account, String otp, OtpCallback callback) {
        // TODO: Implement xác thực OTP
    }

    @Override
    public void resetPassword(String account, String newPassword, ResetPasswordCallback callback) {
        // TODO: Implement đổi mật khẩu
    }

    @Override
    public void logout() {
        // 1. Xóa session trong SharedPreferences
        sharedPreferenceApp.setLoggedIn(false);

        // TODO: 2. Clear Database Room hoặc cache nếu cần thiết tại đây
    }
}