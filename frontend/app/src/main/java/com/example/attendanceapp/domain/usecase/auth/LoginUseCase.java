package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class LoginUseCase {

    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;

    @Inject
    public LoginUseCase(AuthRepository authRepository, AppStateRepository appStateRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
    }

    /**
     * Thực hiện Đăng nhập bằng Số điện thoại và Mật khẩu theo chuẩn RxJava.
     */
    public Single<UserAccount> execute(String phone, String password) {
        if (phone == null || phone.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập số điện thoại"));
        }
        if (password == null || password.isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập mật khẩu"));
        }

        return authRepository.login(phone, password)
                .doOnSuccess(user -> {
                    appStateRepository.setLoggedIn(true);
                    appStateRepository.setAccountSaved(true);
                });
    }

    public Single<Boolean> checkPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập số điện thoại"));
        }
        return authRepository.checkPhoneExists(phone);
    }

    // Xóa các method set trực tiếp, vì UseCase nên quản lý logic này thông qua execute
}
