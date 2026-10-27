package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;

public class GetUserRoleUseCase {

    private final AppStateRepository appStateRepository;

    @Inject
    public GetUserRoleUseCase(AppStateRepository appStateRepository) {
        this.appStateRepository = appStateRepository;
    }

    public UserRole execute() {
        return appStateRepository.getUserRole();
    }
}
