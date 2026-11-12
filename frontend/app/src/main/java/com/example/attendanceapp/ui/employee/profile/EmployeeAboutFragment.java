package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class EmployeeAboutFragment extends Fragment {

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private FragmentAboutBinding binding;

    public EmployeeAboutFragment() {
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
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupListeners() {
        // 1. Chuyển sang màn hình Điều khoản & Chính sách bảo mật (PolicyFragment)
        binding.btnPrivacyPolicy.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeAboutFragment_to_employeePolicyFragment)
        );

        // 2. Mở ứng dụng Email để gửi yêu cầu hỗ trợ kỹ thuật
        binding.btnContactSupport.setOnClickListener(v -> {
            String supportEmail = SupportUiHelper.resolveOrDefault(
                    sharedPreferenceApp != null ? sharedPreferenceApp.getCompanyEmail() : null,
                    SupportUiHelper.DEFAULT_SUPPORT_EMAIL
            );
            SupportUiHelper.sendSupportEmail(
                    requireContext(),
                    supportEmail,
                    "[AttendanceApp] Báo lỗi / Yêu cầu hỗ trợ"
            );
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}