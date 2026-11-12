package com.example.attendanceapp.ui.manager.profile;

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
import com.example.attendanceapp.core.utils.SupportUiHelper;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentAboutBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerAboutFragment extends Fragment {

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

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
        SupportUiHelper.bindAppVersion(requireContext(), binding.tvAppVersion);
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

    private void setupListeners() {
        // Điều hướng trực tiếp đến ID điểm đến trong cùng đồ thị nav_manager để tránh lỗi Action
        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.action_managerAboutFragment_to_managerPolicyFragment);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Không thể mở chính sách bảo mật", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnContactSupport.setOnClickListener(v -> {
            String supportEmail = SupportUiHelper.resolveOrDefault(
                    sharedPreferenceApp != null ? sharedPreferenceApp.getCompanyEmail() : null,
                    SupportUiHelper.DEFAULT_SUPPORT_EMAIL
            );
            SupportUiHelper.sendSupportEmail(
                    requireContext(),
                    supportEmail,
                    "[AttendanceApp] Báo lỗi / Yêu cầu hỗ trợ (Quản lý)"
            );
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
