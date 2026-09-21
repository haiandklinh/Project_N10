package com.example.attendanceapp.ui.employee.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentSettingsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupSwitches();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Cài đặt & Quyền riêng tư");
        binding.layoutToolbar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupSwitches() {
        // Đặt trạng thái ban đầu (Có thể lấy từ SharedPreferences)
        binding.switchAttendanceReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Đã bật nhắc nhở chấm công" : "Đã tắt nhắc nhở chấm công";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        binding.switchHrNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Đã bật thông báo từ HR" : "Đã tắt thông báo từ HR";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Đã kích hoạt xác thực vân tay / khuôn mặt" : "Đã hủy sinh trắc học";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        // 1. Mở cài đặt quyền ứng dụng trong App Settings của hệ điều hành
        binding.btnManagePermissions.setOnClickListener(v -> openAppSettings());

        // 2. Chuyển sang màn hình Điều khoản & Chính sách bảo mật (PrivacyPolicyFragment)
        binding.btnPrivacyPolicy.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_policyFragment)
        );

        // 3. Chuyển sang màn hình Đổi mật khẩu
        binding.btnChangePassword.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_changePasswordFragment)
        );
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}