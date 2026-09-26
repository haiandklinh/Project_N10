package com.example.attendanceapp.domain.repository;

public interface AppStateRepository {
    boolean isLoggedIn();
    void setLoggedIn(boolean isLoggedIn);
    boolean isAccountSaved();
    void setAccountSaved(boolean isSaved);
    boolean isFirstLaunch();
    void setFirstLaunchDone();
}