package com.example.attendanceapp.ui.employee;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

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
        // Sử dụng instance sharedPreferences được Inject trực tiếp bởi Hilt để tránh lệch pha dữ liệu tên file
        String theme = "avocado";
        if (sharedPreferences != null) {
            theme = sharedPreferences.getString("app_theme", "avocado");
        } else {
            android.content.SharedPreferences pref = getSharedPreferences("SHARE_PREFERENCES_APP", MODE_PRIVATE);
            theme = pref.getString("app_theme", "avocado");
        }
        applyAppTheme(theme);

        super.onCreate(savedInstanceState);
        
        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(EmployeeMainViewModel.class);

        // 3. UI Setup
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
        // Sửa lỗi: Gán đúng chiều cao thanh điều hướng hệ thống (navigationBars) vào BottomAppBar để không bị đè che khuất nút bấm
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
            if (binding == null || viewModel == null) return;
            boolean isFullscreen = viewModel.isFullscreenDestination(destination.getId());
            updateBottomBarVisibility(!isFullscreen);
        });

        // Xử lý chuyển tab an toàn khi đang ở CheckIn nhằm tránh lỗi chồng tầng điều hướng
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (navController != null && navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == R.id.employeeCheckInFragment) {
                navController.popBackStack(R.id.employeeHomeFragment, false);
                navController.navigate(item.getItemId());
                return true;
            }
            return navController != null && NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    private void updateBottomBarVisibility(boolean show) {
        if (binding == null) return;
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

            // Chống lỗi crash đúp: Không cho phép navigate tiếp nếu đang đứng sẵn ở màn hình Check-in
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == R.id.employeeCheckInFragment) {
                return;
            }

            navController.navigate(R.id.employeeCheckInFragment);
        });
    }

    @Override
    public void onBackPressed() {
        // Tối ưu hóa phím bấm back điều hướng mượt mà cho trải nghiệm dummy UI
        if (navController != null && navController.getCurrentDestination() != null) {
            int currentId = navController.getCurrentDestination().getId();
            if (currentId == R.id.employeeCheckInFragment) {
                navController.popBackStack(R.id.employeeHomeFragment, false);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}