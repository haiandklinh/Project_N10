package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

public class GetSavedAccountUseCase {

    private final SessionRepository sessionRepository;

    @Inject
    public GetSavedAccountUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SavedAccountData execute() {
        return sessionRepository.getSavedAccountData().blockingGet();
    }
}
