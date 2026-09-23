package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

public class LogoutUseCase {
    private final AuthRepository authRepository;

    @Inject
    public LogoutUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(AuthCallback<Void> callback) {
        authRepository.logout(new AuthRepository.DataCallback<Void>() {
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
}