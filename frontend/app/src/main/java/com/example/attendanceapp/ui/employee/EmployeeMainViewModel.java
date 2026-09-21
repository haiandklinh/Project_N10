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
            R.id.employeeAdjustmentFragment,
            R.id.employeePayrollFragment,
            R.id.employeePayrollDetailFragment,
            R.id.employeeLeaveRequestFragment,
            R.id.employeeShiftExchangeFragment,
            R.id.employeeHelpCenterFragment,
            R.id.employeeAboutFragment,
            R.id.employeeJobInfoFragment,
            R.id.employeeEditProfileFragment,
            R.id.employeePolicyFragment,
            R.id.employeeCreateChangeRequestFragment,
            R.id.employeeSettingsFragment,
            R.id.employeeChangePasswordFragment,
            R.id.employeeChangeRequestHistoryFragment,
            R.id.employeeEditBasicInfoFragment,
            R.id.employeeContactInfoFragment,
            R.id.employeeBankAndTaxFragment,
            R.id.employeeLeaveHistoryFragment,
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