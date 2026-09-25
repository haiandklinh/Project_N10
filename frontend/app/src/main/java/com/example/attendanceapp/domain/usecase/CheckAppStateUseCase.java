package com.example.attendanceapp.domain.usecase;

import com.example.attendanceapp.domain.model.SplashDestination;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;

public class CheckAppStateUseCase {

    private final AppStateRepository repository;

    @Inject
    public CheckAppStateUseCase(AppStateRepository repository) {
        this.repository = repository;
    }

    public SplashDestination execute() {
        boolean isLoggedIn = repository.isLoggedIn();
        boolean isAccountSaved = repository.isAccountSaved();
        boolean isFirstLaunch = repository.isFirstLaunch();

        android.util.Log.d("APP_STATE_DEBUG", "isLoggedIn: " + isLoggedIn
                + " | isAccountSaved: " + isAccountSaved
                + " | isFirstLaunch: " + isFirstLaunch);

        if (isLoggedIn) {
            String status = repository.getUserStatus();
            if ("PENDING_APPROVAL".equals(status)) {
                return SplashDestination.PENDING_APPROVAL;
            }
            return SplashDestination.HOME;
        }
        if (isAccountSaved) {
            return SplashDestination.LOGIN_SAVED_ACCOUNT;
        }
        
        // Nếu chưa từng login hoặc lưu tài khoản (chưa register/login) thì luôn hiển thị Onboarding
        return SplashDestination.ONBOARDING;
    }
}