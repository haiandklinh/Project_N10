package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.domain.model.OtpPurpose;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class RegisterUseCase {
    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;
    private final SessionRepository sessionRepository;
    private final TokenManager tokenManager;

    @Inject
    public RegisterUseCase(
            AuthRepository authRepository,
            AppStateRepository appStateRepository,
            SessionRepository sessionRepository,
            TokenManager tokenManager
    ) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
        this.sessionRepository = sessionRepository;
        this.tokenManager = tokenManager;
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa
     */
    public Single<Boolean> checkPhoneExists(String phoneNumber) {
        return authRepository.checkPhoneExists(phoneNumber);
    }

    /**
     * Gửi mã OTP về Số điện thoại
     */
    public Single<String> sendOtp(String phoneNumber) {
        return authRepository.sendOtp(phoneNumber, OtpPurpose.REGISTER);
    }

    /**
     * Xác thực mã OTP cho Đăng ký
     */
    public Single<String> verifyOtp(String account, String otp) {
        return authRepository.verifyOtp(account, otp, OtpPurpose.REGISTER);
    }

    /**
     * Đăng ký tài khoản mới và xóa sạch dữ liệu tài khoản cũ trong bộ nhớ local.
     */
    public Completable execute(String phoneNumber, String password) {
        return authRepository.register(phoneNumber, password)
                .andThen(Completable.fromAction(() -> {
                    tokenManager.clearTokens();
                    appStateRepository.clearSession();
                    appStateRepository.setAccountSaved(false);
                    appStateRepository.saveUserStatus(UserStatus.NEEDS_ROLE_SELECTION);
                }))
                .andThen(sessionRepository.resetForNewAccount(phoneNumber));
    }
}
