package com.example.attendanceapp.ui.manager.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAboutBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerAboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    public ManagerAboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        displayAppVersion();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Giới thiệu ứng dụng");
        binding.layoutToolbar.setOnBackClickListener(() -> {
            try {
                Navigation.findNavController(requireView()).navigateUp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void displayAppVersion() {
        try {
            PackageManager pm = requireContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(requireContext().getPackageName(), 0);
            String version = pInfo.versionName;
            binding.tvAppVersion.setText("Phiên bản " + version);
        } catch (Exception e) {
            e.printStackTrace();
            binding.tvAppVersion.setText("Phiên bản 1.0.0");
        }
    }

    private void setupListeners() {
        // Điều hướng trực tiếp đến ID điểm đến trong cùng đồ thị nav_manager để tránh lỗi Action
        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.managerPolicyFragment);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Không thể mở chính sách bảo mật", Toast.LENGTH_SHORT).show();
            }
        });

        // Mở ứng dụng Email an toàn có try-catch tránh crash khi thiếu ứng dụng mail (ví dụ máy ảo)
        binding.btnContactSupport.setOnClickListener(v -> sendSupportEmail());
    }

    private void sendSupportEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@attendanceapp.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "[AttendanceApp] Báo lỗi / Yêu cầu hỗ trợ (Quản lý)");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng email trên thiết bị", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
