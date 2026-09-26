package com.example.attendanceapp.ui.main.auth.login;

import android.content.Intent;
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
import com.example.attendanceapp.databinding.FragmentPendingApprovalBinding;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PendingApprovalFragment extends Fragment {

    private FragmentPendingApprovalBinding binding;
    private String inviteCode;
    private String companyName;

    public static PendingApprovalFragment newInstance(String inviteCode, String companyName) {
        PendingApprovalFragment fragment = new PendingApprovalFragment();
        Bundle args = new Bundle();
        args.putString("invite_code", inviteCode);
        args.putString("company_name", companyName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            inviteCode = getArguments().getString("invite_code", "");
            companyName = getArguments().getString("company_name", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPendingApprovalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        // Display invite code and company name
        binding.tvInviteCode.setText(inviteCode);
        binding.tvCompanyName.setText(companyName);

        binding.btnCheckStatus.setOnClickListener(v -> checkApprovalStatus());
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void checkApprovalStatus() {
        // TODO: Call API to check approval status
        // For now, simulate checking
        binding.btnCheckStatus.setEnabled(false);
        
        // Simulate API call
        binding.btnCheckStatus.postDelayed(() -> {
            binding.btnCheckStatus.setEnabled(true);
            
            // TODO: Replace with actual API response
            boolean isApproved = false; // Change to true when approved
            
            if (isApproved) {
                // Navigate to EmployeeMainActivity
                navigateToEmployeeHome();
            } else {
                Toast.makeText(requireContext(), "Yêu cầu của bạn vẫn đang chờ quản lý duyệt", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    private void navigateToEmployeeHome() {
        Intent intent = new Intent(requireContext(), EmployeeMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void logout() {
        // TODO: Clear saved user data and navigate back to login
        Navigation.findNavController(requireView()).navigate(R.id.action_pendingApprovalFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
