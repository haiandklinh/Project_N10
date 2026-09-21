package com.example.attendanceapp.ui.main.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.SplashDestination;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.CheckAppStateUseCase;
import com.example.attendanceapp.domain.usecase.auth.GetUserRoleUseCase;


import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SplashViewModel extends ViewModel {

    private final CheckAppStateUseCase checkAppStateUseCase;
    private final GetUserRoleUseCase getUserRoleUseCase; // UseCase lấy role hiện tại
    private final MutableLiveData<SplashDestination> destination = new MutableLiveData<>();

    @Inject
    public SplashViewModel(CheckAppStateUseCase checkAppStateUseCase,
                           GetUserRoleUseCase getUserRoleUseCase) {
        this.checkAppStateUseCase = checkAppStateUseCase;
        this.getUserRoleUseCase = getUserRoleUseCase;
    }

    public LiveData<SplashDestination> getDestination() {
        return destination;
    }

    public void checkAppState() {
        SplashDestination result = checkAppStateUseCase.execute();
        destination.setValue(result);
    }

    /**
     * Lấy vai trò của người dùng hiện tại từ Domain/Data Layer
     */
    public UserRole getUserRole() {
        return getUserRoleUseCase.execute();
    }
}