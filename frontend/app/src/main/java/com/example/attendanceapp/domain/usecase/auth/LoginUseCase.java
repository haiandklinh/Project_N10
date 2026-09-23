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
        authRepository.login(phone, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess(UserAccount user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void setLoggedIn(boolean loggedIn) {
        appStateRepository.setLoggedIn(loggedIn);
    }

    public void setAccountSaved(boolean saved) {
        appStateRepository.setAccountSaved(saved);
    }

    // Bổ sung vào LoginUseCase.java
    public SavedAccountData getSavedAccount() {
        return appStateRepository.getSavedAccountData(); // Đọc từ SharedPreferences
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}