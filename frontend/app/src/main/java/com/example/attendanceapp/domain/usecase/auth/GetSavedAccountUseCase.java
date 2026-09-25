package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;

public class GetSavedAccountUseCase {

    private final AppStateRepository appStateRepository;

    @Inject
    public GetSavedAccountUseCase(AppStateRepository appStateRepository) {
        this.appStateRepository = appStateRepository;
    }

    public SavedAccountData execute() {
        return appStateRepository.getSavedAccountData();
    }
}
