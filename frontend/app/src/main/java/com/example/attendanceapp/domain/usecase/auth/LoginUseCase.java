package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

public class LoginUseCase {

    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;

    @Inject
    public LoginUseCase(AuthRepository authRepository, AppStateRepository appStateRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
    }

    /**
     * Thực hiện Đăng nhập bằng Số điện thoại và Mật khẩu.
     */
    public void execute(String phone, String password, AuthCallback<UserAccount> callback) {
        if (phone == null || phone.trim().isEmpty()) {
            if (callback != null) callback.onError("Vui lòng nhập số điện thoại");
            return;
        }
        if (password == null || password.isEmpty()) {
            if (callback != null) callback.onError("Vui lòng nhập mật khẩu");
            return;
        }

        authRepository.login(phone, password, new AuthRepository.DataCallback<UserAccount>() {
            @Override
            public void onSuccess(UserAccount user) {
                appStateRepository.setLoggedIn(true);
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    public void setLoggedIn(boolean loggedIn) {
        appStateRepository.setLoggedIn(loggedIn);
    }

    public void setAccountSaved(boolean saved) {
        appStateRepository.setAccountSaved(saved);
    }
}