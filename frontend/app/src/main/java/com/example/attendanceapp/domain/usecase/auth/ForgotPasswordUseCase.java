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
        authRepository.sendOtp(account, new AuthRepository.OtpCallback() {
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
    public void verifyOtp(String account, String otp, AuthCallback<Void> callback) {
        authRepository.verifyOtp(account, otp, new AuthRepository.OtpCallback() {
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
     * Đặt lại mật khẩu mới
     */
    public void resetPassword(String account, String newPassword, AuthCallback<Void> callback) {
        authRepository.resetPassword(account, newPassword, new AuthRepository.ResetPasswordCallback() {
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

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
