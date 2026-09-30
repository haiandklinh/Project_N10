package com.example.attendanceapp.ui.employee.schedule.leave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentLeaveHistoryBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class EmployeeLeaveHistoryFragment extends Fragment {

    private FragmentLeaveHistoryBinding binding;
    private EmployeeLeaveRequestAdapter adapter;
    private final List<LeaveRequest> allRequests = new ArrayList<>();
    private final List<LeaveRequest> displayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaveHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();
        setupTabs();
        setupSwipeRefresh();
        setupListeners();
        loadData();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Lịch sử đơn từ");
        binding.toolbarApp.setBackButtonVisible(true);
        binding.toolbarApp.setOnBackClickListener(() -> Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new EmployeeLeaveRequestAdapter(displayList);
        binding.rvLeaveRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLeaveRequests.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterData(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void setupListeners() {
        binding.fabCreateRequest.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeLeaveHistoryFragment_to_employeeLeaveRequestFragment)
        );
    }

    private void loadData() {
        binding.swipeRefreshLayout.setRefreshing(true);

        // Mock data - Sử dụng Enum ApprovalRequestType và ApprovalStatus
        allRequests.clear();
        allRequests.add(new LeaveRequest("1", "user1", ApprovalRequestType.LEAVE, "20/05/2026", null, "Việc gia đình", false, ApprovalStatus.APPROVED, System.currentTimeMillis()));
        allRequests.add(new LeaveRequest("2", "user1", ApprovalRequestType.LEAVE, "22/05/2026", null, "Sốt xuất huyết", false, ApprovalStatus.PENDING, System.currentTimeMillis()));
        allRequests.add(new LeaveRequest("3", "user1", ApprovalRequestType.SWAP, "25/05/2026", "S1", "Đi đám cưới", false, ApprovalStatus.REJECTED, System.currentTimeMillis()));
        allRequests.add(new LeaveRequest("4", "user1", ApprovalRequestType.LEAVE, "28/05/2026", null, "Giải quyết việc riêng", false, ApprovalStatus.PENDING, System.currentTimeMillis()));

        filterData(binding.tabLayoutStatus.getSelectedTabPosition());

        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void filterData(int tabPosition) {
        displayList.clear();
        if (tabPosition == 0) { // Tab "Tất cả"
            displayList.addAll(allRequests);
        } else {
            ApprovalStatus targetStatus;
            switch (tabPosition) {
                case 1:
                    targetStatus = ApprovalStatus.PENDING; // Tab "Chờ duyệt"
                    break;
                case 2:
                    targetStatus = ApprovalStatus.APPROVED; // Tab "Đã duyệt"
                    break;
                case 3:
                default:
                    targetStatus = ApprovalStatus.REJECTED; // Tab "Từ chối"
                    break;
            }

            for (LeaveRequest req : allRequests) {
                if (req.getStatus() == targetStatus) {
                    displayList.add(req);
                }
            }
        }

        adapter.updateData(displayList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (displayList.isEmpty()) {
            binding.layoutEmptyState.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvLeaveRequests.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.layoutEmpty.setVisibility(View.GONE);
            binding.rvLeaveRequests.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}