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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAuthPendingApprovalBinding;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PendingApprovalFragment extends Fragment {

    private FragmentAuthPendingApprovalBinding binding;
    private PendingApprovalViewModel viewModel;

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
        binding = FragmentAuthPendingApprovalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PendingApprovalViewModel.class);

        setupViews();
        observeViewModel();
    }

    private void setupViews() {
        // Hiển thị thông tin mã mời và tên công ty
        binding.tvInviteCode.setText(inviteCode != null ? inviteCode : "");
        binding.tvCompanyName.setText(companyName != null ? companyName : "");

        binding.btnCheckStatus.setOnClickListener(v -> viewModel.checkApprovalStatus());
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void observeViewModel() {
        // Observe trạng thái Loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCheckStatus.setEnabled(!isLoading);
            binding.btnLogout.setEnabled(!isLoading);
        });

        // Observe kết quả kiểm tra phê duyệt
        viewModel.getApprovalStatusState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof PendingApprovalViewModel.ApprovalStatusState.Approved) {
                Toast.makeText(requireContext(), "Chúc mừng! Yêu cầu của bạn đã được phê duyệt", Toast.LENGTH_SHORT).show();
                navigateToEmployeeHome();
            } else if (state instanceof PendingApprovalViewModel.ApprovalStatusState.Pending) {
                Toast.makeText(requireContext(), "Yêu cầu của bạn vẫn đang chờ quản lý duyệt", Toast.LENGTH_SHORT).show();
            } else if (state instanceof PendingApprovalViewModel.ApprovalStatusState.Error) {
                String error = ((PendingApprovalViewModel.ApprovalStatusState.Error) state).message;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToEmployeeHome() {
        Intent intent = new Intent(requireContext(), EmployeeMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void logout() {
        // 1. Xóa sạch Session đăng nhập trong SharedPreferences
        viewModel.logout();

        // 2. Điều hướng quay về màn hình Đăng nhập
        Navigation.findNavController(requireView()).navigate(R.id.action_pendingApprovalFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}