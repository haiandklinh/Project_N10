package com.example.attendanceapp.ui.employee;

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
public class EmployeeMainViewModel extends ViewModel {

    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<String> _currentTheme = new MutableLiveData<>();
    public LiveData<String> getCurrentTheme() {
        return _currentTheme;
    }

    // Danh sách các Fragment full-screen (không hiện BottomBar)
    private final List<Integer> fullscreenDestinations = Arrays.asList(
            R.id.splashFragment,
            R.id.loginFragment,
            R.id.forgotPasswordFragment,
            R.id.onboardingFragment,
            R.id.adjustmentFragment,
            R.id.payrollFragment,
            R.id.payrollDetailFragment,
            R.id.leaveRequestFragment,
            R.id.shiftExchangeFragment,
            R.id.helpCenterFragment,
            R.id.aboutFragment,
            R.id.jobInfoFragment,
            R.id.editProfileFragment,
            R.id.policyFragment,
            R.id.createChangeRequestFragment,
            R.id.settingsFragment,
            R.id.changePasswordFragment,
            R.id.changeRequestHistoryFragment,
            R.id.editBasicInfoFragment,
            R.id.contactInfoFragment,
            R.id.bankAndTaxFragment,
            R.id.leaveHistoryFragment,
            R.id.registerFragment
    );

    @Inject
    public EmployeeMainViewModel(SharedPreferences sharedPreferences) {
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