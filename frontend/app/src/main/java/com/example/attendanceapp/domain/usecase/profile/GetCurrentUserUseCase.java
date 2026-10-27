package com.example.attendanceapp.domain.usecase.profile;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Maybe;

/**
 * Lấy thông tin tài khoản người dùng hiện tại đang lưu trong phiên (SessionRepository / Room).
 */
public class GetCurrentUserUseCase {

    private final SessionRepository sessionRepository;

    @Inject
    public GetCurrentUserUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Maybe<UserAccount> execute() {
        return sessionRepository.getCurrentUser();
    }
}
