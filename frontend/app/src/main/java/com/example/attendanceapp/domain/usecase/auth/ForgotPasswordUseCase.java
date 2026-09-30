package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.OtpPurpose;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ForgotPasswordUseCase {

    private final AuthRepository authRepository;

    @Inject
    public ForgotPasswordUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Gửi mã OTP cho luồng Quên mật khẩu (luôn dùng OtpPurpose.FORGOT_PASSWORD)
     */
    public Completable sendOtp(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
            return Completable.error(new IllegalArgumentException("Số điện thoại không hợp lệ"));
        }
        return authRepository.sendOtp(phoneNumber, OtpPurpose.FORGOT_PASSWORD);
    }

    /**
     * Xác thực mã OTP cho luồng Quên mật khẩu (luôn dùng OtpPurpose.FORGOT_PASSWORD)
     */
    public Single<String> verifyOtp(String phoneNumber, String otp) {
        return authRepository.verifyOtp(phoneNumber, otp, OtpPurpose.FORGOT_PASSWORD);
    }

    /**
     * Đặt lại mật khẩu mới
     */
    public Completable resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Token xác thực không hợp lệ"));
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Completable.error(new IllegalArgumentException("Mật khẩu phải chứa ít nhất 6 ký tự"));
        }
        return authRepository.resetPassword(resetToken, newPassword);
    }
}
