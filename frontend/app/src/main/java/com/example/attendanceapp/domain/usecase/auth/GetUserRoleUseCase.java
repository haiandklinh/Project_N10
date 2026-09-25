package com.example.attendanceapp.domain.usecase.auth;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

public class GetUserRoleUseCase {

    private final SessionRepository sessionRepository;

    @Inject
    public GetUserRoleUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public UserRole execute() {
        return sessionRepository.getSavedUserRole();
    }
}