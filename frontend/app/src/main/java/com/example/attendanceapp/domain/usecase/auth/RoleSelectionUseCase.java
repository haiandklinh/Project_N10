package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

public class RoleSelectionUseCase {
    private final AuthRepository authRepository;

    @Inject
    public RoleSelectionUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Chọn vai trò cho người dùng
     */
    public void selectRole(UserRole role, AuthCallback<Void> callback) {
        authRepository.selectRole(role, new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Tạo công ty mới (cho Manager)
     */
    public void createCompany(String companyName, AuthCallback<String> callback) {
        authRepository.createCompany(companyName, new AuthRepository.DataCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Tham gia công ty bằng mã mời (cho Employee)
     */
    public void joinCompanyWithCode(String invitationCode, AuthCallback<String> callback) {
        authRepository.joinCompanyWithCode(invitationCode, new AuthRepository.DataCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
