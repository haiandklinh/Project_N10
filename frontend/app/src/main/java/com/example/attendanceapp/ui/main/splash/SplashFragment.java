package com.example.attendanceapp.ui.main.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAuthSplashBinding;
import com.example.attendanceapp.domain.model.SplashDestination;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {

    private FragmentAuthSplashBinding binding;
    private SplashViewModel viewModel;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedCheckRunnable;

    private static final long SPLASH_DELAY_MS = 1500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        setupObservers();
        startSplashDelay();
    }

    private void setupObservers() {
        viewModel.getDestination().observe(getViewLifecycleOwner(), this::navigateTo);
    }

    private void startSplashDelay() {
        delayedCheckRunnable = viewModel::checkAppState;
        handler.postDelayed(delayedCheckRunnable, SPLASH_DELAY_MS);
    }

    private void navigateTo(SplashDestination destination) {
        if (destination == null || !isAdded()) return;

        NavController navController = NavHostFragment.findNavController(this);

        switch (destination) {
            case ONBOARDING:
                navController.navigate(R.id.action_splashFragment_to_onboardingFragment);
                break;

            case LOGIN_SAVED_ACCOUNT:
                navController.navigate(R.id.action_splashFragment_to_loginSavedAccountFragment);
                break;

            case PENDING_APPROVAL:
                navController.navigate(R.id.action_splashFragment_to_pendingApprovalFragment);
                break;

            case HOME:
                UserRole userRole = viewModel.getUserRole();
                navigateToRoleHome(userRole);
                break;

            case LOGIN:
            default:
                navController.navigate(R.id.action_splashFragment_to_loginFragment);
                break;
        }
    }

    private void navigateToRoleHome(UserRole role) {
        if (role == UserRole.MANAGER) {
            Intent intent = new Intent(requireContext(), ManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        } else {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_splashFragment_to_staffCompanySelectionFragment);
        }
    }

    @Override
    public void onDestroyView() {
        if (delayedCheckRunnable != null) {
            handler.removeCallbacks(delayedCheckRunnable);
        }
        super.onDestroyView();
        binding = null;
    }
}
