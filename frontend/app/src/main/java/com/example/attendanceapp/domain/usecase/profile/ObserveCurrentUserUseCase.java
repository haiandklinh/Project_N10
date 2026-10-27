package com.example.attendanceapp.domain.usecase.profile;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;

/**
 * Lắng nghe thay đổi thông tin tài khoản người dùng hiện tại (SessionRepository / Room).
 */
public class ObserveCurrentUserUseCase {

    private final SessionRepository sessionRepository;

    @Inject
    public ObserveCurrentUserUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Observable<UserAccount> execute() {
        return sessionRepository.observeCurrentUser();
    }
}
