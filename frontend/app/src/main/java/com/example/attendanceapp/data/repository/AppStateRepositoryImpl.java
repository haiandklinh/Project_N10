package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Session state (SharedPreferences): is_logged_in, is_account_saved, is_first_launch, user_role, user_status.
 */
@Singleton
public class AppStateRepositoryImpl implements AppStateRepository {

    private final SharedPreferenceApp sharedPreferenceApp;
    private final BehaviorSubject<Boolean> loggedInSubject;

    @Inject
    public AppStateRepositoryImpl(SharedPreferenceApp sharedPreferenceApp) {
        this.sharedPreferenceApp = sharedPreferenceApp;
        this.loggedInSubject = BehaviorSubject.createDefault(sharedPreferenceApp.isLoggedIn());
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferenceApp.isLoggedIn();
    }

    @Override
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferenceApp.setLoggedIn(isLoggedIn);
        loggedInSubject.onNext(isLoggedIn);
    }

    @Override
    public Observable<Boolean> observeLoggedIn() {
        return loggedInSubject.distinctUntilChanged();
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

    @Override
    public String getUserStatus() {
        return sharedPreferenceApp.getUserStatus();
    }

    @Override
    public void saveUserStatus(UserStatus status) {
        sharedPreferenceApp.saveUserStatus(status);
    }

    @Override
    public UserRole getUserRole() {
        return UserRole.fromString(sharedPreferenceApp.getUserRole());
    }

    @Override
    public void saveUserRole(UserRole role) {
        if (role != null) {
            sharedPreferenceApp.setUserRole(role.getValue());
        }
    }

    @Override
    public void clearSession() {
        sharedPreferenceApp.clearSession();
        sharedPreferenceApp.setLoggedIn(false);
        loggedInSubject.onNext(false);
    }
}