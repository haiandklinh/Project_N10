package com.example.attendanceapp.ui.manager;

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
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManagerMainViewModel.class);

        // 2. Apply Theme từ ViewModel trước super.onCreate()
        applyAppTheme(viewModel.getCurrentTheme().getValue());

        // 3. UI Setup
        EdgeToEdge.enable(this);
        binding = ActivityManagerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppbarManager, (v, insets) -> {
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.getPaddingLeft();
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
                .findFragmentById(R.id.nav_host_fragment_manager);
        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigationManager, navController);

        // Lắng nghe đổi màn hình để Ẩn/Hiện BottomBar dựa trên ViewModel Logic
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isFullscreen = viewModel.isFullscreenDestination(destination.getId());
            updateBottomBarVisibility(!isFullscreen);
        });
    }

    private void updateBottomBarVisibility(boolean show) {
        if (show) {
            binding.bottomAppbarManager.setVisibility(View.VISIBLE);
            binding.bottomNavigationManager.setVisibility(View.VISIBLE);
//            binding.fabManagerAction.show();
        } else {
            binding.bottomAppbarManager.setVisibility(View.GONE);
            binding.bottomNavigationManager.setVisibility(View.GONE);
//            binding.fabManagerAction.hide();
        }
    }

    private void setupFab() {
//        binding.fabManagerAction.setOnClickListener(v -> {
//            // Xử lý sự kiện click nút FAB nhanh của Manager ở đây (Ví dụ: Thêm nhân viên/Tạo thông báo)
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}