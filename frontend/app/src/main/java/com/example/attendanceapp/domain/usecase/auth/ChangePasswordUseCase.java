package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.data.dto.request.auth.LoginRequest;
import com.example.attendanceapp.data.dto.request.auth.ResetPasswordRequest;
import com.example.attendanceapp.data.dto.response.auth.MessageResponse;
import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.data.remote.AuthApi;
import com.example.attendanceapp.domain.exception.AppException;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ChangePasswordUseCase {

    private final AuthApi authApi;
    private final SessionRepository sessionRepository;
    private final TokenManager tokenManager;

    @Inject
    public ChangePasswordUseCase(
            AuthApi authApi,
            SessionRepository sessionRepository,
            TokenManager tokenManager
    ) {
        this.authApi = authApi;
        this.sessionRepository = sessionRepository;
        this.tokenManager = tokenManager;
    }

    public Completable execute(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại"));
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Completable.error(new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự"));
        }
        if (newPassword.equals(currentPassword)) {
            return Completable.error(new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại"));
        }

        // 1. Lấy số điện thoại tài khoản đang đăng nhập
        return getLoggedInPhoneNumber()
                .flatMapCompletable(phoneNumber ->
                        // 2. Kiểm tra mật khẩu hiện tại bằng cách gọi API login
                        authApi.login(new LoginRequest(phoneNumber, currentPassword))
                                .onErrorResumeNext(throwable -> {
                                    if (throwable instanceof retrofit2.HttpException) {
                                        int code = ((retrofit2.HttpException) throwable).code();
                                        if (code == 401 || code == 400) {
                                            return Single.error(new AppException("Mật khẩu hiện tại không chính xác", 401, "INVALID_CURRENT_PASSWORD"));
                                        }
                                    }
                                    return Single.error(throwable);
                                })
                                // 3. Cập nhật mật khẩu mới lên máy chủ
                                .flatMap(authResponse -> authApi.resetPassword(new ResetPasswordRequest(phoneNumber, newPassword)))
                                .flatMapCompletable(messageResponse -> {
                                    if (messageResponse != null && "Reset Success".equalsIgnoreCase(messageResponse.getMessage())) {
                                        // 4. Lấy lại token mới cho phiên làm việc hiện tại (do server đã revoke token cũ)
                                        return authApi.login(new LoginRequest(phoneNumber, newPassword))
                                                .doOnSuccess(newAuth -> {
                                                    tokenManager.saveTokens(newAuth.getAccessToken(), newAuth.getRefreshToken());
                                                })
                                                .ignoreElement()
                                                .onErrorComplete();
                                    } else {
                                        String msg = (messageResponse != null && messageResponse.getMessage() != null)
                                                ? messageResponse.getMessage()
                                                : "Đổi mật khẩu thất bại";
                                        return Completable.error(new AppException(msg, 400, "CHANGE_PASSWORD_FAILED"));
                                    }
                                })
                );
    }

    private Single<String> getLoggedInPhoneNumber() {
        return sessionRepository.getCurrentUser()
                .map(user -> user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : "")
                .filter(phone -> !phone.isEmpty())
                .switchIfEmpty(sessionRepository.getSavedAccountData()
                        .map(data -> data.phone != null ? data.phone.trim() : "")
                        .filter(phone -> !phone.isEmpty()))
                .switchIfEmpty(Single.error(new AppException("Không tìm thấy thông tin tài khoản đang đăng nhập", 404, "USER_NOT_FOUND")));
    }
}
