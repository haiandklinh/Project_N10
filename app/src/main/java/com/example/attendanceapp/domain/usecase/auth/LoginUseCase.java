package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import java.time.Instant;

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
     * Dang nhap bang Ma cong ty + So dien thoai + Mat khau.
     * companyCode dung de backend xac dinh dung tenant truoc khi kiem tra tai khoan,
     * vi cung 1 so dien thoai co the ton tai o nhieu cong ty khac nhau.
     */
    public void execute(String companyCode, String phone, String password,
                        AuthCallback<UserAccount> callback) {
        // TODO: Goi API dang nhap that su qua AuthRepository, vi du:
        // authRepository.login(companyCode, phone, password, callback);

        // Tam thoi mock du lieu tra ve
        callback.onSuccess(new UserAccount(
                "mock-user-id",
                phone,
                phone,
                UserRole.EMPLOYEE,
                false,
                true,
                Instant.now()
        ));
    }

    public void setLoggedIn(boolean loggedIn) {
        appStateRepository.setLoggedIn(loggedIn);
    }

    public void setAccountSaved(boolean saved) {
        appStateRepository.setAccountSaved(saved);
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}