package com.example.attendanceapp.ui.manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ActivityManagerMainBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerMainActivity extends AppCompatActivity {

    @Inject
    SharedPreferences sharedPreferences;

    private ActivityManagerMainBinding binding;
    private NavController navController;
    private ManagerMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Đọc theme trực tiếp từ SharedPreferences TRƯỚC super.onCreate()
        SharedPreferences pref = getSharedPreferences("SHARE_PREFERENCES_APP", MODE_PRIVATE);
        String theme = pref.getString("app_theme", "avocado");
        applyAppTheme(theme);

        super.onCreate(savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManagerMainViewModel.class);

        // 2. UI Setup
        binding = ActivityManagerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupCustomBottomNavListeners();
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


    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_manager);
        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();

        // Đồng bộ trạng thái tab khi chuyển màn hình
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            // Highlight tab tương ứng với màn hình
            if (id == R.id.managerHomeFragment) {
                updateSelectedTab(binding.tabHome);
            } else if (id == R.id.managerApprovalFragment) {
                updateSelectedTab(binding.tabApproval);
            } else if (id == R.id.managerScheduleFragment) {
                updateSelectedTab(binding.tabSchedule);
            } else if (id == R.id.managerHumanFragment) {
                updateSelectedTab(binding.tabEmployee);
            } else if (id == R.id.managerProfileFragment) {
                updateSelectedTab(binding.tabProfile);
            }

            // Ẩn/Hiện Custom BottomBar dựa trên ViewModel Logic
            boolean isFullscreen = viewModel.isFullscreenDestination(id);
            updateBottomBarVisibility(!isFullscreen);
        });
    }

    private void setupCustomBottomNavListeners() {
        // Tab 1: Dashboard / Home
        binding.tabHome.setOnClickListener(v -> navigateToTab(R.id.managerHomeFragment));

        // Tab 2: Approvals
        binding.tabApproval.setOnClickListener(v -> navigateToTab(R.id.managerApprovalFragment));

        // Tab 3: Schedule
        binding.tabSchedule.setOnClickListener(v -> navigateToTab(R.id.managerScheduleFragment));

        // Tab 4: Employees
        binding.tabEmployee.setOnClickListener(v -> navigateToTab(R.id.managerHumanFragment));

        // Tab 5: Profile
        binding.tabProfile.setOnClickListener(v -> navigateToTab(R.id.managerProfileFragment));
    }

    private void navigateToTab(int destinationId) {
        if (navController == null) return;

        // Bỏ qua nếu đang đứng ở chính destination đó
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) {
            return;
        }

        // Cấu hình NavOptions chuẩn cho Bottom Navigation (Không đè lặp BackStack)
        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(
                        R.id.managerHomeFragment,
                        false,
                        true
                )
                .build();

        navController.navigate(destinationId, null, navOptions);
    }

    private void updateSelectedTab(View selectedTab) {
        // Tab 1
        boolean isHome = (selectedTab == binding.tabHome);
        binding.tabHome.setSelected(isHome);
        binding.iconHome.setSelected(isHome);
        binding.labelHome.setSelected(isHome);

        // Tab 2
        boolean isApproval = (selectedTab == binding.tabApproval);
        binding.tabApproval.setSelected(isApproval);
        binding.iconApproval.setSelected(isApproval);
        binding.labelApproval.setSelected(isApproval);

        // Tab 3
        boolean isSchedule = (selectedTab == binding.tabSchedule);
        binding.tabSchedule.setSelected(isSchedule);
        binding.iconSchedule.setSelected(isSchedule);
        binding.labelSchedule.setSelected(isSchedule);

        // Tab 4
        boolean isEmployee = (selectedTab == binding.tabEmployee);
        binding.tabEmployee.setSelected(isEmployee);
        binding.iconEmployee.setSelected(isEmployee);
        binding.labelEmployee.setSelected(isEmployee);

        // Tab 5
        boolean isProfile = (selectedTab == binding.tabProfile);
        binding.tabProfile.setSelected(isProfile);
        binding.iconProfile.setSelected(isProfile);
        binding.labelProfile.setSelected(isProfile);
    }

    private void updateBottomBarVisibility(boolean show) {
        binding.bottomNavigation.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}