package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

public class ForgotPasswordUseCase {

    private final AuthRepository authRepository;

    @Inject
    public ForgotPasswordUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Gửi mã OTP về số điện thoại/email
     */
    public void sendOtp(String account, AuthCallback<Void> callback) {
        authRepository.sendOtp(account, new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Xác thực mã OTP
     */
    public void verifyOtp(String account, String otp, AuthCallback<Void> callback) {
        authRepository.verifyOtp(account, otp, new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Đặt lại mật khẩu mới
     */
    public void resetPassword(String account, String newPassword, AuthCallback<Void> callback) {
        authRepository.resetPassword(account, newPassword, new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
