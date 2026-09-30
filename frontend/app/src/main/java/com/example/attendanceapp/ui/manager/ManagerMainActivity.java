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
        // Sử dụng sharedPreferences được inject để đồng bộ cấu hình app
        String theme = "avocado";
        if (sharedPreferences != null) {
            theme = sharedPreferences.getString("app_theme", "avocado");
        } else {
            // Fallback nếu inject gặp vấn đề (hiếm gặp với Hilt)
            SharedPreferences pref = getSharedPreferences("SHARE_PREFERENCES_APP", MODE_PRIVATE);
            theme = pref.getString("app_theme", "avocado");
        }
        applyAppTheme(theme);

        super.onCreate(savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManagerMainViewModel.class);

        // 2. UI Setup
        binding = ActivityManagerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Chạy hiệu ứng mở rèm nếu vừa đổi Dark Mode
        com.example.attendanceapp.core.utils.DarkModeAnimationHelper.checkAndRunOpenCurtainAnimation(this);

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
            if (binding == null) return;
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
            if (viewModel != null) {
                boolean isFullscreen = viewModel.isFullscreenDestination(id);
                updateBottomBarVisibility(!isFullscreen);
            }
        });
    }

    private void setupCustomBottomNavListeners() {
        // Gán sự kiện click cho các custom tab với check null an toàn
        if (binding.tabHome != null) binding.tabHome.setOnClickListener(v -> navigateToTab(R.id.managerHomeFragment));
        if (binding.tabApproval != null) binding.tabApproval.setOnClickListener(v -> navigateToTab(R.id.managerApprovalFragment));
        if (binding.tabSchedule != null) binding.tabSchedule.setOnClickListener(v -> navigateToTab(R.id.managerScheduleFragment));
        if (binding.tabEmployee != null) binding.tabEmployee.setOnClickListener(v -> navigateToTab(R.id.managerHumanFragment));
        if (binding.tabProfile != null) binding.tabProfile.setOnClickListener(v -> navigateToTab(R.id.managerProfileFragment));
    }

    public void navigateToTab(int destinationId) {
        if (navController == null) return;

        // Bỏ qua nếu đang đứng ở chính destination đó (Tránh lặp điều hướng)
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) {
            return;
        }

        // Cấu hình NavOptions chuẩn cho Bottom Navigation
        // Tối ưu: setSaveState(true) để giữ trạng thái các tab khi chuyển đổi
        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(
                        R.id.managerHomeFragment,
                        false,
                        true // quan trọng: saveState = true
                )
                .build();

        navController.navigate(destinationId, null, navOptions);
    }

    private void updateSelectedTab(View selectedTab) {
        if (binding == null) return;

        // Tab 1: Dashboard
        boolean isHome = (selectedTab == binding.tabHome);
        binding.tabHome.setSelected(isHome);
        binding.iconHome.setSelected(isHome);
        binding.labelHome.setSelected(isHome);

        // Tab 2: Approvals
        boolean isApproval = (selectedTab == binding.tabApproval);
        binding.tabApproval.setSelected(isApproval);
        binding.iconApproval.setSelected(isApproval);
        binding.labelApproval.setSelected(isApproval);

        // Tab 3: Schedule
        boolean isSchedule = (selectedTab == binding.tabSchedule);
        binding.tabSchedule.setSelected(isSchedule);
        binding.iconSchedule.setSelected(isSchedule);
        binding.labelSchedule.setSelected(isSchedule);

        // Tab 4: Employees
        boolean isEmployee = (selectedTab == binding.tabEmployee);
        binding.tabEmployee.setSelected(isEmployee);
        binding.iconEmployee.setSelected(isEmployee);
        binding.labelEmployee.setSelected(isEmployee);

        // Tab 5: Profile
        boolean isProfile = (selectedTab == binding.tabProfile);
        binding.tabProfile.setSelected(isProfile);
        binding.iconProfile.setSelected(isProfile);
        binding.labelProfile.setSelected(isProfile);
    }

    private void updateBottomBarVisibility(boolean show) {
        if (binding != null && binding.bottomNavigation != null) {
            binding.bottomNavigation.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (navController != null) {
            // 1. Nếu có thể quay lại trong backstack của fragment hiện tại (ví dụ từ sub-fragment về Home)
            if (navController.popBackStack()) {
                return;
            }
            // 2. Nếu đang ở gốc của tab khác, nhấn Back sẽ quay về tab Home trước khi thoát
            if (navController.getCurrentDestination() != null) {
                int currentId = navController.getCurrentDestination().getId();
                if (currentId != R.id.managerHomeFragment) {
                    navigateToTab(R.id.managerHomeFragment);
                    return;
                }
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