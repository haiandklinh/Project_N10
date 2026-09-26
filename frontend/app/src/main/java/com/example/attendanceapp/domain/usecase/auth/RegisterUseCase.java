package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import java.time.Instant;

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
     * Dang ky tai khoan Admin/nguoi dai dien cong ty.
     * Khong con role/contact/isEmail: nguoi dang ky qua flow nay luon la ADMIN,
     * lien he chi con so dien thoai (phone).
     */
    public void execute(String fullName, String companyName, String phone, String password,
                        AuthCallback<UserAccount> callback) {
        // TODO: Goi API dang ky that su qua AuthRepository, vi du:
        // authRepository.register(fullName, companyName, phone, password, callback);

        // Tam thoi mock du lieu tra ve
        callback.onSuccess(new UserAccount(
                "mock-user-id",
                fullName,
                phone,
                UserRole.MANAGER,
                true,
                true,
                Instant.now()
        ));
    }

    public void saveAccountSaved() {
        appStateRepository.setAccountSaved(true);
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}