package com.example.attendanceapp.ui.main.auth;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ThemeViewModel quản lý theme của ứng dụng.
 * Được đổi tên từ AuthViewModel vì chỉ quản lý theme, không có logic Auth.
 */
@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<String> _currentTheme = new MutableLiveData<>();

    public LiveData<String> getCurrentTheme() {
        return _currentTheme;
    }

    @Inject
    public AuthViewModel(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        loadAppTheme();
    }

    private void loadAppTheme() {
        String theme = sharedPreferences.getString("app_theme", "avocado");
        _currentTheme.setValue(theme);
    }

    public void setTheme(String theme) {
        sharedPreferences.edit().putString("app_theme", theme).apply();
        _currentTheme.setValue(theme);
    }
}
