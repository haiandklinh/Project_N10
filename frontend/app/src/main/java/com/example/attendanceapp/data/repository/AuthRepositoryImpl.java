package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.auth.*;
import com.example.attendanceapp.data.dto.response.auth.*;
import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.AuthApi;
import com.example.attendanceapp.domain.exception.AppException;
import com.example.attendanceapp.domain.model.OtpPurpose;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final AuthApi authApi;
    private final TokenManager tokenManager;
    private final SessionRepository sessionRepository;
    private final AppStateRepository appStateRepository;

    @Inject
    public AuthRepositoryImpl(AuthApi authApi,
                              TokenManager tokenManager,
                              SessionRepository sessionRepository,
                              AppStateRepository appStateRepository) {
        this.authApi = authApi;
        this.tokenManager = tokenManager;
        this.sessionRepository = sessionRepository;
        this.appStateRepository = appStateRepository;
    }

    @Override
    public Single<Boolean> checkPhoneExists(String phoneNumber) {
        return authApi.checkPhone(new CheckPhoneRequest(phoneNumber))
                .map(CheckPhoneResponse::isExist)
                .compose(ApiErrorMapper.forSingle("Kiểm tra số điện thoại thất bại"));
    }

    @Override
    public Completable register(String phoneNumber, String password) {
        return authApi.register(new RegisterRequest(phoneNumber, password))
                .compose(ApiErrorMapper.forCompletable("Đăng ký thất bại"));
    }

    @Override
    public Completable login(String phoneNumber, String password) {
        return authApi.login(new LoginRequest(phoneNumber, password))
                .flatMapCompletable(auth -> {
                    tokenManager.saveTokens(auth.getAccessToken(), auth.getRefreshToken());
                    appStateRepository.setLoggedIn(true);
                    // Lưu số điện thoại vào Room để giữ lại khi save profile
                    return sessionRepository.savePhoneNumber(phoneNumber.trim());
                })
                .doOnError(error -> clearLocalTokensAndState())
                .compose(ApiErrorMapper.forCompletable("Số điện thoại hoặc mật khẩu không chính xác"));
    }

    @Override
    public Completable logout() {
        return Completable.defer(() -> {
            String refreshToken = tokenManager.getRefreshToken();
            Completable remote = (refreshToken == null)
                    ? Completable.complete()
                    : authApi.logout(new RefreshTokenRequest(refreshToken)).onErrorComplete();

            return remote
                    .andThen(Completable.fromAction(this::clearLocalTokensAndState))
                    .andThen(sessionRepository.clear());
        });
    }

    @Override
    public Single<String> sendOtp(String phoneNumber, OtpPurpose purpose) {
        if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
            return Single.error(new IllegalArgumentException("Số điện thoại không hợp lệ"));
        }

        SendOtpRequest request = new SendOtpRequest(phoneNumber);
        Single<SendOtpResponse> call = (purpose == OtpPurpose.REGISTER)
                ? authApi.sendRegisterOtp(request)
                : authApi.sendForgotPasswordOtp(request);

        return call.map(res -> res.getOtp() != null ? res.getOtp() : "")
                .compose(ApiErrorMapper.forSingle("Gửi OTP thất bại"));
    }

    @Override
    public Single<String> verifyOtp(String phoneNumber, String otp, OtpPurpose purpose) {
        VerifyOtpRequest request = new VerifyOtpRequest(phoneNumber, otp);
        Single<VerifyOtpResponse> call = (purpose == OtpPurpose.REGISTER)
                ? authApi.verifyRegisterOtp(request)
                : authApi.verifyForgotPasswordOtp(request);

        return call.flatMap(res -> res.isVerified()
                        ? Single.just(res.getResetToken() != null ? res.getResetToken() : phoneNumber)
                        : Single.<String>error(new AppException("Mã OTP không đúng hoặc đã hết hạn", 400, "INVALID_OTP")))
                .compose(ApiErrorMapper.forSingle("Mã OTP không đúng hoặc đã hết hạn"));
    }

    @Override
    public Completable resetPassword(String phoneNumber, String newPassword) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Số điện thoại không hợp lệ"));
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Completable.error(new IllegalArgumentException("Mật khẩu phải chứa ít nhất 6 ký tự"));
        }

        return authApi.resetPassword(new ResetPasswordRequest(phoneNumber, newPassword))
                .compose(ApiErrorMapper.forCompletable("Đặt lại mật khẩu thất bại"));
    }

    // --- Private helpers ---

    private void clearLocalTokensAndState() {
        tokenManager.clearTokens();
        appStateRepository.clearSession(); // đã set logged-in = false và phát lại observeLoggedIn()
    }
}