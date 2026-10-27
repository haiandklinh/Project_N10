package com.example.attendanceapp.ui.manager.human;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerJoinRequestBinding;
import com.example.attendanceapp.domain.model.JoinRequestItem;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerJoinRequestFragment extends Fragment {

    private FragmentManagerJoinRequestBinding binding;
    private ManagerHumanViewModel viewModel;
    private ManagerJoinRequestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerJoinRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Shared ViewModel to update badge count & sync invitation code
        viewModel = new ViewModelProvider(requireActivity()).get(ManagerHumanViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        
        // Gọi load dữ liệu thực tế từ backend
        viewModel.loadJoinRequests();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle(getString(R.string.human_join_requests_title));
    }

    private void setupListeners() {
        if (binding.btnJoinCopyCode != null) {
            binding.btnJoinCopyCode.setOnClickListener(v -> {
                String code = binding.tvJoinInviteCode.getText().toString();
                if ("------".equals(code) || code.isEmpty()) {
                    CompanyInvitationBottomSheet.newInstance().show(getChildFragmentManager(), CompanyInvitationBottomSheet.TAG);
                    return;
                }
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Invite Code", code);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), getString(R.string.manager_profile_copy_success), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (binding.cardJoinInviteCode != null) {
            binding.cardJoinInviteCode.setOnClickListener(v -> {
                CompanyInvitationBottomSheet.newInstance().show(getChildFragmentManager(), CompanyInvitationBottomSheet.TAG);
            });
        }
    }

    private void observeViewModel() {
        // Đồng bộ mã mời
        viewModel.invitationCode.observe(getViewLifecycleOwner(), code -> {
            if (code != null && !code.trim().isEmpty()) {
                binding.tvJoinInviteCode.setText(code);
            } else {
                binding.tvJoinInviteCode.setText("------");
            }
        });

        // Đồng bộ danh sách đơn xin gia nhập từ backend
        viewModel.joinRequests.observe(getViewLifecycleOwner(), list -> {
            List<JoinRequestItem> safeList = (list != null) ? list : new ArrayList<>();
            adapter.updateData(safeList);
            updateEmptyState(safeList.isEmpty());
        });

        // Báo lỗi nếu có
        viewModel.errorMessage.observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvJoinRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new ManagerJoinRequestAdapter(new ArrayList<>(), new ManagerJoinRequestAdapter.OnActionListener() {
            @Override
            public void onApprove(JoinRequestItem item, int position) {
                AssignPositionBottomSheet.newInstance(item.getId(), item.getFullName(), item.getAvatarUrl(), (staffId, fullName, avatarUrl, assignedPosition) -> {
                    viewModel.handleJoinRequest(staffId, fullName, avatarUrl, assignedPosition, true, () -> {
                        Toast.makeText(requireContext(), "Đã duyệt nhân sự " + fullName + " với chức vụ " + assignedPosition, Toast.LENGTH_SHORT).show();
                    });
                }).show(getChildFragmentManager(), AssignPositionBottomSheet.TAG);
            }

            @Override
            public void onReject(JoinRequestItem item, int position) {
                viewModel.handleJoinRequest(item.getId(), item.getFullName(), item.getAvatarUrl(), "", false, () -> {
                    Toast.makeText(requireContext(), "Đã từ chối yêu cầu của " + item.getFullName(), Toast.LENGTH_SHORT).show();
                });
            }
        });
        
        binding.rvJoinRequests.setAdapter(adapter);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (binding == null) return;
        if (isEmpty) {
            binding.rvJoinRequests.setVisibility(View.GONE);
            binding.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
            binding.includeEmptyState.tvEmptyTitle.setText("Không có yêu cầu nào");
            binding.includeEmptyState.tvEmptySubtitle.setText("Chia sẻ mã mời của bạn để nhân viên gửi yêu cầu tham gia công ty.");
        } else {
            binding.rvJoinRequests.setVisibility(View.VISIBLE);
            binding.includeEmptyState.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadJoinRequests();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
