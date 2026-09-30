package com.example.attendanceapp.ui.manager;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.R;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ManagerMainViewModel extends ViewModel {

    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<String> _currentTheme = new MutableLiveData<>();
    public LiveData<String> getCurrentTheme() {
        return _currentTheme;
    }

    // Danh sách các màn hình Fullscreen không hiện BottomBar trong luồng Manager
    private final List<Integer> fullscreenDestinations = Arrays.asList(
            R.id.managerJoinRequestFragment,
            R.id.managerPayrollFragment,
            R.id.managerRewardPunishFragment,
            R.id.managerStatisticFragment,
            R.id.assignShiftFragment,
            R.id.assignShiftSuccessFragment,
            R.id.selectEmployeeFragment

    );

    @Inject
    public ManagerMainViewModel(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        loadAppTheme();
    }

    private void loadAppTheme() {
        String theme = sharedPreferences.getString("app_theme", "avocado");
        _currentTheme.setValue(theme);
    }

    public boolean isFullscreenDestination(int destinationId) {
        return fullscreenDestinations.contains(destinationId);
    }
}