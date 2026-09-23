package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

public class RegisterUseCase {
    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;

    @Inject
    public RegisterUseCase(AuthRepository authRepository, AppStateRepository appStateRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
    }

    /**
     * Gửi mã OTP về Số điện thoại
     */
    public void sendOtp(String phoneNumber, AuthCallback<Void> callback) {
        authRepository.sendOtp(phoneNumber, new AuthRepository.OtpCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Xác thực mã OTP
     */
    public void verifyOtp(String phoneNumber, String otp, AuthCallback<Void> callback) {
        authRepository.verifyOtp(phoneNumber, otp, new AuthRepository.OtpCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Đăng ký tài khoản cơ bản (Chỉ còn Họ tên, SĐT, Mật khẩu)
     */
    public void execute(String fullName, String phoneNumber, String password, AuthCallback<UserAccount> callback) {
        authRepository.register(fullName, phoneNumber, password, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess(UserAccount user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void saveAccountSaved() {
        appStateRepository.setAccountSaved(true);
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}