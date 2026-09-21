package com.example.attendanceapp.ui.employee.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAboutBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    public AboutFragment() {
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
        binding.layoutToolbar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void displayAppVersion() {
        try {
            PackageManager pm = requireContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(requireContext().getPackageName(), 0);
            String version = pInfo.versionName;
            binding.tvAppVersion.setText("Phiên bản " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            binding.tvAppVersion.setText("Phiên bản 1.0.0");
        }
    }

    private void setupListeners() {
        // 1. Chuyển sang màn hình Điều khoản & Chính sách bảo mật (PolicyFragment)
        binding.btnPrivacyPolicy.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_aboutFragment_to_policyFragment)
        );

        // 2. Mở ứng dụng Email để gửi yêu cầu hỗ trợ kỹ thuật
        binding.btnContactSupport.setOnClickListener(v -> sendSupportEmail());
    }

    private void sendSupportEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@attendanceapp.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "[AttendanceApp] Báo lỗi / Yêu cầu hỗ trợ");

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}