package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;

public class AppStateRepositoryImpl implements AppStateRepository {
    private final SharedPreferenceApp sharedPreferenceApp;

    @Inject
    public AppStateRepositoryImpl(SharedPreferenceApp sharedPreferenceApp) {
        this.sharedPreferenceApp = sharedPreferenceApp;
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferenceApp.isLoggedIn();
    }

    @Override
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferenceApp.setLoggedIn(isLoggedIn);
    }

    @Override
    public boolean isAccountSaved() {
        return sharedPreferenceApp.isAccountSaved();
    }

    @Override
    public void setAccountSaved(boolean isSaved) {
        sharedPreferenceApp.setAccountSaved(isSaved);
    }

    @Override
    public boolean isFirstLaunch() {
        return sharedPreferenceApp.isFirstLaunch();
    }

    @Override
    public void setFirstLaunchDone() {
        sharedPreferenceApp.setFirstLaunchDone();
    }
}