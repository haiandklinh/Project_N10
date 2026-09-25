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
     * Gửi mã OTP cho luồng Quên mật khẩu (luôn dùng OtpPurpose.FORGOT_PASSWORD)
     */
    public void sendOtp(String phoneNumber, AuthCallback<Void> callback) {
        if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
            if (callback != null) callback.onError("Số điện thoại không hợp lệ");
            return;
        }

        authRepository.sendOtp(phoneNumber, AuthRepository.OtpPurpose.FORGOT_PASSWORD, new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (callback != null) callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    /**
     * Xác thực mã OTP cho luồng Quên mật khẩu (luôn dùng OtpPurpose.FORGOT_PASSWORD)
     */
    public void verifyOtp(String phoneNumber, String otp, AuthCallback<String> callback) {
        authRepository.verifyOtp(phoneNumber, otp, AuthRepository.OtpPurpose.FORGOT_PASSWORD, new AuthRepository.DataCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (callback != null) callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    /**
     * Đặt lại mật khẩu mới
     */
    public void resetPassword(String phoneNumber, String newPassword, AuthCallback<Void> callback) {
        if (newPassword == null || newPassword.length() < 6) {
            if (callback != null) callback.onError("Mật khẩu phải chứa ít nhất 6 ký tự");
            return;
        }

        authRepository.resetPassword(phoneNumber, newPassword, new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (callback != null) callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }
}