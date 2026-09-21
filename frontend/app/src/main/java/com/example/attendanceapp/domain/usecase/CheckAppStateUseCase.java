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
            return SplashDestination.HOME;
        }
        if (isAccountSaved) {
            return SplashDestination.LOGIN_SAVED_ACCOUNT;
        }
        if (isFirstLaunch) {
            repository.setFirstLaunchDone();
            return SplashDestination.ONBOARDING;
        }
        return SplashDestination.LOGIN;
    }
}