package com.example.attendanceapp.domain.usecase.auth;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.UserRepository; // Hoặc AuthRepository

import javax.inject.Inject;

public class GetUserRoleUseCase {

    private final UserRepository userRepository;

    @Inject
    public GetUserRoleUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRole execute() {
        return userRepository.getSavedUserRole();
    }
}