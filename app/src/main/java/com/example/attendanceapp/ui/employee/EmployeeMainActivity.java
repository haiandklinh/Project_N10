package com.example.attendanceapp.ui.employee;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ActivityEmployeeMainBinding;
import com.example.attendanceapp.ui.employee.profile.DarkModeAnimationHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeMainActivity extends AppCompatActivity {

    @Inject
    SharedPreferences sharedPreferences;

    private ActivityEmployeeMainBinding binding;
    private NavController navController;
    private EmployeeMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(EmployeeMainViewModel.class);

        // 2. Apply Theme từ ViewModel trước super.onCreate()
        applyAppTheme(viewModel.getCurrentTheme().getValue());

        // 3. UI Setup
        EdgeToEdge.enable(this);
        binding = ActivityEmployeeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DarkModeAnimationHelper.checkAndRunOpenCurtainAnimation(this);

        setupWindowInsets();
        setupNavigation();
        setupFab();
    }

    private void applyAppTheme(String theme) {
        if (theme == null) theme = "avocado";
        switch (theme) {
            case "watermelon":
                setTheme(R.style.Theme_AttendanceApp_Watermelon);
                break;
            case "orange":
                setTheme(R.style.Theme_AttendanceApp_Orange);
                break;
            default:
                setTheme(R.style.Theme_AttendanceApp_Avocado);
                break;
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppbar, (v, insets) -> {
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    navigationBars.bottom
            );
            return insets;
        });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // Lắng nghe đổi màn hình để Ẩn/Hiện BottomBar dựa trên ViewModel Logic
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isFullscreen = viewModel.isFullscreenDestination(destination.getId());
            updateBottomBarVisibility(!isFullscreen);
        });

        // Xử lý chuyển tab khi đang ở CheckIn
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == R.id.checkInFragment) {
                navController.popBackStack(R.id.homeFragment, false);
                navController.navigate(item.getItemId());
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    private void updateBottomBarVisibility(boolean show) {
        if (show) {
            binding.bottomAppbar.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setVisibility(View.VISIBLE);
            binding.fabAttendance.show();
        } else {
            binding.bottomAppbar.setVisibility(View.GONE);
            binding.bottomNavigation.setVisibility(View.GONE);
            binding.fabAttendance.hide();
        }
    }

    private void setupFab() {
        binding.fabAttendance.setOnClickListener(v -> {
            if (navController == null) return;

            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == R.id.checkInFragment) {
                return;
            }

            navController.navigate(R.id.checkInFragment);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}