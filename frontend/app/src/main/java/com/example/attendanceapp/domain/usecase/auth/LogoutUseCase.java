package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class LogoutUseCase {
    private final AuthRepository authRepository;

    @Inject
    public LogoutUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Thực hiện đăng xuất theo chuẩn RxJava.
     */
    public Completable execute() {
        return authRepository.logout();
    }
}
