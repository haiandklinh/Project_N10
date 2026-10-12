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
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.JoinRequestItem;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerJoinRequestFragment extends Fragment {

    private FragmentManagerJoinRequestBinding binding;
    private ManagerHumanViewModel viewModel;
    private ManagerJoinRequestAdapter adapter;
    private List<JoinRequestItem> sampleRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerJoinRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Shared ViewModel to update badge count
        viewModel = new ViewModelProvider(requireActivity()).get(ManagerHumanViewModel.class);

        setupToolbar();
        setupRecyclerView();
        loadJoinRequests();
        setupListeners();
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
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invite Code", binding.tvJoinInviteCode.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), getString(R.string.manager_profile_copy_success), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRecyclerView() {
        binding.rvJoinRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        sampleRequests = new ArrayList<>();
        
        adapter = new ManagerJoinRequestAdapter(sampleRequests, new ManagerJoinRequestAdapter.OnActionListener() {
            @Override
            public void onApprove(JoinRequestItem item, int position) {
                Toast.makeText(requireContext(), "Đã chấp nhận nhân viên " + item.getFullName() + " vào doanh nghiệp", Toast.LENGTH_SHORT).show();
                removeItem(position);
            }

            @Override
            public void onReject(JoinRequestItem item, int position) {
                Toast.makeText(requireContext(), "Đã từ chối yêu cầu của " + item.getFullName(), Toast.LENGTH_SHORT).show();
                removeItem(position);
            }
        });
        
        binding.rvJoinRequests.setAdapter(adapter);
    }

    private void removeItem(int position) {
        if (sampleRequests != null && position >= 0 && position < sampleRequests.size()) {
            sampleRequests.remove(position);
            adapter.updateData(new ArrayList<>(sampleRequests));
            updateEmptyState();
            viewModel.setJoinRequestCount(sampleRequests.size());
        }
    }

    private void updateEmptyState() {
        if (binding == null) return;
        if (sampleRequests == null || sampleRequests.isEmpty()) {
            binding.rvJoinRequests.setVisibility(View.GONE);
            binding.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
            binding.includeEmptyState.tvEmptyTitle.setText("Không có yêu cầu nào");
            binding.includeEmptyState.tvEmptySubtitle.setText("Tất cả đơn xin gia nhập đã được xử lý.");
        } else {
            binding.rvJoinRequests.setVisibility(View.VISIBLE);
            binding.includeEmptyState.getRoot().setVisibility(View.GONE);
        }
    }

    private void loadJoinRequests() {
        sampleRequests.clear();
        // Mock data cho danh sách đơn xin gia nhập đang chờ duyệt
        sampleRequests.add(new JoinRequestItem("1", "Nguyễn Văn A", null, "0901234567", "BKS-2024", "2 phút trước", ApprovalStatus.PENDING));
        sampleRequests.add(new JoinRequestItem("2", "Phạm Thanh Sơn", null, "0988776655", "BKS-CODE-2024", "10 phút trước", ApprovalStatus.PENDING));
        sampleRequests.add(new JoinRequestItem("3", "Trần Thị B", null, "0977665544", "BKS-2024", "15 phút trước", ApprovalStatus.PENDING));
        sampleRequests.add(new JoinRequestItem("4", "Lê Thị Hồng", null, "0912345678", "BKS-CODE-2024", "30 phút trước", ApprovalStatus.PENDING));
        sampleRequests.add(new JoinRequestItem("5", "Hoàng Văn C", null, "0966554433", "BKS-2024", "1 giờ trước", ApprovalStatus.PENDING));

        adapter.updateData(new ArrayList<>(sampleRequests));
        updateEmptyState();
        viewModel.setJoinRequestCount(sampleRequests.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

