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
import com.example.attendanceapp.core.utils.DarkModeAnimationHelper;

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
        // Đọc theme trực tiếp từ SharedPreferences TRƯỚC super.onCreate() để setTheme có tác dụng
        android.content.SharedPreferences pref = getSharedPreferences("SHARE_PREFERENCES_APP", MODE_PRIVATE);
        String theme = pref.getString("app_theme", "avocado");
        applyAppTheme(theme);

        super.onCreate(savedInstanceState);
        
        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(EmployeeMainViewModel.class);

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
            case "pink":
                setTheme(R.style.Theme_AttendanceApp_Pink);
                break;
            case "blueberry":
                setTheme(R.style.Theme_AttendanceApp_Blueberry);
                break;
            case "chocolate":
                setTheme(R.style.Theme_AttendanceApp_Chocolate);
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
                    v.getPaddingBottom()
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
                    && navController.getCurrentDestination().getId() == R.id.employeeCheckInFragment) {
                navController.popBackStack(R.id.employeeHomeFragment, false);
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
                    && navController.getCurrentDestination().getId() == R.id.employeeCheckInFragment) {
                return;
            }

            navController.navigate(R.id.employeeCheckInFragment);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}