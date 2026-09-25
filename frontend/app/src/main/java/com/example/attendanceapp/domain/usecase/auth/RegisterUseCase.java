package com.example.attendanceapp.domain.usecase.auth;

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
     * Kiểm tra số điện thoại đã tồn tại chưa
     */
    public void checkPhoneExists(String phoneNumber, AuthCallback<Boolean> callback) {
        authRepository.checkPhoneExists(phoneNumber, new AuthRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (callback != null) callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    /**
     * Gửi mã OTP về Số điện thoại
     */
    public void sendOtp(String phoneNumber, AuthCallback<Void> callback) {
        authRepository.sendOtp(phoneNumber, AuthRepository.OtpPurpose.REGISTER, new AuthRepository.DataCallback<Void>() {
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
     * Xác thực mã OTP cho Đăng ký
     */
    public void verifyOtp(String account, String otp, AuthCallback<String> callback) {
        authRepository.verifyOtp(account, otp, AuthRepository.OtpPurpose.REGISTER, new AuthRepository.DataCallback<String>() {
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
     * Đăng ký tài khoản (Khớp với AuthRepository.register trả về Void)
     */
    public void execute(String phoneNumber, String password, AuthCallback<Void> callback) {
        authRepository.register(phoneNumber, password, new AuthRepository.DataCallback<Void>() {
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

    public void saveAccountSaved() {
        appStateRepository.setAccountSaved(true);
    }
}