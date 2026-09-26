package com.example.attendanceapp.ui.main.onboarding;

import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.repository.AppStateRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final AppStateRepository appStateRepository;

    @Inject
    public OnboardingViewModel(AppStateRepository appStateRepository) {
        this.appStateRepository = appStateRepository;
    }

    public void completeOnboarding() {
        appStateRepository.setFirstLaunchDone();
    }
}