package com.example.attendanceapp.ui.main.auth.company;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentStaffCompanySelectionBinding;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;
import com.example.attendanceapp.ui.main.auth.login.EnterInvitationCodeBottomSheet;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StaffCompanySelectionFragment extends Fragment {

    @Inject
    AppStateRepository appStateRepository;

    private FragmentStaffCompanySelectionBinding binding;
    private StaffCompanySelectionViewModel viewModel;
    private StaffCompanyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStaffCompanySelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nếu người dùng có vai trò Quản lý thì vào thẳng app Quản lý, không hiện màn hình chọn công ty
        if (appStateRepository != null && appStateRepository.getUserRole() == UserRole.MANAGER) {
            Intent intent = new Intent(requireContext(), ManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(StaffCompanySelectionViewModel.class);

        setupRecyclerView();
        setupListeners();
        setupFragmentResultListener();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new StaffCompanyAdapter(company -> viewModel.selectCompany(company));
        binding.rvCompanies.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCompanies.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadCompanies());

        binding.btnJoinAnotherCompany.setOnClickListener(v -> showEnterCodeBottomSheet());
        binding.btnEmptyJoinCode.setOnClickListener(v -> showEnterCodeBottomSheet());

        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> viewModel.logout())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showEnterCodeBottomSheet() {
        EnterInvitationCodeBottomSheet.newInstance()
                .show(getChildFragmentManager(), EnterInvitationCodeBottomSheet.TAG);
    }

    private void setupFragmentResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                EnterInvitationCodeBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(EnterInvitationCodeBottomSheet.RESULT_ACTION, "");
                    if (EnterInvitationCodeBottomSheet.ACTION_SUBMITTED.equals(action)) {
                        String invitationCode = result.getString(EnterInvitationCodeBottomSheet.BUNDLE_KEY_CODE, "");
                        if (!invitationCode.isEmpty()) {
                            viewModel.joinCompanyWithCode(invitationCode);
                        }
                    }
                }
        );
    }

    private void observeViewModel() {
        viewModel.companies.observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvCompanies.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvCompanies.setVisibility(View.VISIBLE);
                adapter.setItems(list);
            }
        });

        viewModel.userName.observe(getViewLifecycleOwner(), name -> {
            if (name != null && !name.isEmpty()) {
                binding.tvWelcomeTitle.setText("Chào mừng, " + name);
            }
        });

        viewModel.isRefreshing.observe(getViewLifecycleOwner(), refreshing -> {
            binding.swipeRefreshLayout.setRefreshing(refreshing);
        });

        viewModel.isActionLoading.observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnJoinAnotherCompany.setEnabled(!loading);
            binding.btnLogout.setEnabled(!loading);
            binding.rvCompanies.setEnabled(!loading);
            binding.btnEmptyJoinCode.setEnabled(!loading);
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.companySelectedEvent.observe(getViewLifecycleOwner(), company -> {
            if (company != null) {
                viewModel.consumeCompanySelectedEvent();
                Intent intent = new Intent(requireContext(), EmployeeMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        viewModel.joinSuccessEvent.observe(getViewLifecycleOwner(), managerInfo -> {
            if (managerInfo != null) {
                viewModel.consumeJoinSuccessEvent();
                String companyName = managerInfo.getCompanyName() != null ? managerInfo.getCompanyName() : "công ty";
                Toast.makeText(requireContext(), "Đã gửi yêu cầu tham gia " + companyName, Toast.LENGTH_SHORT).show();

                Bundle bundle = new Bundle();
                bundle.putString("invite_code", viewModel.getLastSubmittedCode());
                bundle.putString("company_name", companyName);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_staffCompanySelectionFragment_to_pendingApprovalFragment, bundle);
            }
        });

        viewModel.logoutSuccess.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_staffCompanySelectionFragment_to_loginFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
