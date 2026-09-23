package com.example.attendanceapp.domain.usecase.auth;

public interface AuthCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}