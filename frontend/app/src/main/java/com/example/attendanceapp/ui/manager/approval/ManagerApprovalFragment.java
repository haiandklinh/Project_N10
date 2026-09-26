package com.example.attendanceapp.ui.manager.approval;

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

import com.example.attendanceapp.databinding.FragmentManagerApprovalBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerApprovalFragment extends Fragment {

    private FragmentManagerApprovalBinding binding;
    private ManagerApprovalViewModel viewModel;

    private final List<ManagerApprovalItem> allRequests = new ArrayList<>();
    private ManagerApprovalAdapter adapter;

    private int selectedStatusTab = 0; // 0 = Chờ duyệt, 1 = Đã duyệt, 2 = Từ chối, 3 = Tất cả
    private ApprovalRequestType selectedType = null; // null = tất cả loại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerApprovalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerApprovalViewModel.class);

        binding.toolbarApp.setTitle("Duyệt yêu cầu");
        binding.toolbarApp.setBackButtonVisible(false);

        loadMockData();
        setupList();
        setupStatusTabs();
        setupTypeChips();
        setupSwipeRefresh();

        applyFilters();
    }

    // GHI CHÚ: Danh sách mock để dựng UI, khi nối API thay bằng dữ liệu thật từ UseCase.
    private void loadMockData() {
        allRequests.add(new ManagerApprovalItem("1", "Trần Thị Bình", null, "Nhân viên bán hàng",
                ApprovalRequestType.LEAVE, "Ngày áp dụng: 20/09/2026", "Nghỉ cả ngày",
                "Bị ốm đột xuất cần nghỉ để đi khám bệnh.", ManagerApprovalItem.STATUS_PENDING, 3));
        allRequests.add(new ManagerApprovalItem("2", "Lê Văn Cường", null, "Nhân viên kho",
                ApprovalRequestType.ADJUSTMENT, "Ngày áp dụng: 19/09/2026", "Bổ sung check-out",
                "Quên bấm check-out lúc tan ca do sự cố máy chấm công.", ManagerApprovalItem.STATUS_PENDING, 2));
        allRequests.add(new ManagerApprovalItem("3", "Phạm Thu Hà", null, "Thu ngân",
                ApprovalRequestType.SWAP, "Ngày áp dụng: 22/09/2026", "Đổi sang Ca Chiều",
                "Có việc gia đình buổi sáng nên xin đổi sang ca chiều.", ManagerApprovalItem.STATUS_PENDING, 1));
        allRequests.add(new ManagerApprovalItem("4", "Đỗ Minh Khoa", null, "Nhân viên bảo vệ",
                ApprovalRequestType.LEAVE, "Ngày áp dụng: 15/09/2026", "Nghỉ nửa ngày sáng",
                "Đi khám sức khỏe định kỳ.", ManagerApprovalItem.STATUS_APPROVED, 5));
        allRequests.add(new ManagerApprovalItem("5", "Ngô Thị Lan", null, "Nhân viên pha chế",
                ApprovalRequestType.ADJUSTMENT, "Ngày áp dụng: 14/09/2026", "Chỉnh giờ check-in",
                "Check-in bị sai do định vị GPS chập chờn.", ManagerApprovalItem.STATUS_REJECTED, 6));
    }

    private void setupList() {
        adapter = new ManagerApprovalAdapter(new ArrayList<>(), new ManagerApprovalAdapter.OnActionListener() {
            @Override
            public void onApprove(ManagerApprovalItem item, int position) {
                item.setStatus(ManagerApprovalItem.STATUS_APPROVED);
                Toast.makeText(getContext(), "Đã duyệt yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                applyFilters();
            }

            @Override
            public void onReject(ManagerApprovalItem item, int position) {
                item.setStatus(ManagerApprovalItem.STATUS_REJECTED);
                Toast.makeText(getContext(), "Đã từ chối yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                applyFilters();
            }
        });
        binding.rvApprovalRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvApprovalRequests.setAdapter(adapter);
    }

    private void setupStatusTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedStatusTab = tab.getPosition();
                applyFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupTypeChips() {
        binding.chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedType = null;
            } else if (checkedIds.get(0) == binding.chipTypeLeave.getId()) {
                selectedType = ApprovalRequestType.LEAVE;
            } else if (checkedIds.get(0) == binding.chipTypeAdjustment.getId()) {
                selectedType = ApprovalRequestType.ADJUSTMENT;
            } else if (checkedIds.get(0) == binding.chipTypeSwap.getId()) {
                selectedType = ApprovalRequestType.SWAP;
            } else {
                selectedType = null;
            }
            applyFilters();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            applyFilters();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void applyFilters() {
        List<ManagerApprovalItem> filtered = new ArrayList<>();
        for (ManagerApprovalItem item : allRequests) {
            boolean matchStatus;
            switch (selectedStatusTab) {
                case 1:
                    matchStatus = ManagerApprovalItem.STATUS_APPROVED.equals(item.getStatus());
                    break;
                case 2:
                    matchStatus = ManagerApprovalItem.STATUS_REJECTED.equals(item.getStatus());
                    break;
                case 3:
                    matchStatus = true;
                    break;
                default:
                    matchStatus = ManagerApprovalItem.STATUS_PENDING.equals(item.getStatus());
                    break;
            }
            boolean matchType = (selectedType == null) || (item.getType() == selectedType);
            if (matchStatus && matchType) {
                filtered.add(item);
            }
        }

        adapter.updateData(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.rvApprovalRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
