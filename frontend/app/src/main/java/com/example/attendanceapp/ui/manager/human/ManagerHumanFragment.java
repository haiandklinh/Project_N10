package com.example.attendanceapp.ui.manager.human;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.databinding.FragmentManagerHumanBinding;
import com.example.attendanceapp.domain.model.JoinRequestItem;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHumanFragment extends Fragment {

    private FragmentManagerHumanBinding binding;
    private ManagerHumanViewModel viewModel;

    private final List<ManagerEmployeeItem> employees = new ArrayList<>();
    private final List<JoinRequestItem> joinRequests = new ArrayList<>();

    private ManagerEmployeeAdapter employeeAdapter;
    private ManagerJoinRequestAdapter joinRequestAdapter;

    private int selectedTab = 0; // 0 = Nhân viên, 1 = Yêu cầu tham gia
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerHumanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerHumanViewModel.class);

        binding.toolbarApp.setTitle("Nhân sự");
        binding.toolbarApp.setBackButtonVisible(false);

        loadMockData();
        setupAdapters();
        setupTabs();
        setupSearch();
        setupSwipeRefresh();

        applyFilters();
    }

    private void loadMockData() {
        employees.add(new ManagerEmployeeItem("u1", "NV001", "Trần Thị Bình", null,
                "Nhân viên bán hàng", "Cửa hàng Q1", "0901 111 222", "03/01/2025",
                ManagerEmployeeItem.STATUS_ACTIVE, 20, 1, 7200000));
        employees.add(new ManagerEmployeeItem("u2", "NV002", "Lê Văn Cường", null,
                "Nhân viên kho", "Kho vận", "0902 222 333", "12/03/2024",
                ManagerEmployeeItem.STATUS_ACTIVE, 22, 0, 7850000));
        employees.add(new ManagerEmployeeItem("u3", "NV003", "Phạm Thu Hà", null,
                "Thu ngân", "Cửa hàng Q1", "0903 333 444", "20/06/2025",
                ManagerEmployeeItem.STATUS_ACTIVE, 18, 2, 6500000));
        employees.add(new ManagerEmployeeItem("u4", "NV004", "Đỗ Minh Khoa", null,
                "Nhân viên bảo vệ", "Cửa hàng Q3", "0904 444 555", "05/11/2023",
                ManagerEmployeeItem.STATUS_ACTIVE, 24, 0, 8100000));
        employees.add(new ManagerEmployeeItem("u5", "NV005", "Ngô Thị Lan", null,
                "Nhân viên pha chế", "Cửa hàng Q1", "0905 555 666", "18/08/2025",
                ManagerEmployeeItem.STATUS_ON_LEAVE, 15, 3, 5200000));

        joinRequests.add(new JoinRequestItem("j1", "Vũ Đức Anh", null, "0912 345 678",
                "NV-8823", "12 phút trước", JoinRequestItem.STATUS_PENDING));
        joinRequests.add(new JoinRequestItem("j2", "Bùi Thị Mai", null, "0987 654 321",
                "NV-8823", "Hôm qua", JoinRequestItem.STATUS_PENDING));
    }

    private void setupAdapters() {
        employeeAdapter = new ManagerEmployeeAdapter(new ArrayList<>(), employee ->
                EmployeeDetailBottomSheet.newInstance(employee).show(getChildFragmentManager(), "employee_detail"));

        joinRequestAdapter = new ManagerJoinRequestAdapter(new ArrayList<>(), new ManagerJoinRequestAdapter.OnActionListener() {
            @Override
            public void onApprove(JoinRequestItem item, int position) {
                joinRequests.remove(item);
                Toast.makeText(getContext(), "Đã chấp nhận " + item.getFullName() + " vào công ty", Toast.LENGTH_SHORT).show();
                applyFilters();
            }

            @Override
            public void onReject(JoinRequestItem item, int position) {
                joinRequests.remove(item);
                Toast.makeText(getContext(), "Đã từ chối yêu cầu của " + item.getFullName(), Toast.LENGTH_SHORT).show();
                applyFilters();
            }
        });

        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvList.setAdapter(employeeAdapter);
    }

    private void setupTabs() {
        binding.tabLayoutHuman.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                binding.rvList.setAdapter(selectedTab == 0 ? employeeAdapter : joinRequestAdapter);
                binding.tilSearch.setVisibility(selectedTab == 0 ? View.VISIBLE : View.GONE);
                applyFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupSearch() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                applyFilters();
            }

            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            applyFilters();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void applyFilters() {
        boolean isEmpty;
        if (selectedTab == 0) {
            List<ManagerEmployeeItem> filtered = new ArrayList<>();
            for (ManagerEmployeeItem employee : employees) {
                boolean matches = searchQuery.isEmpty()
                        || employee.getFullName().toLowerCase(Locale.ROOT).contains(searchQuery)
                        || employee.getEmployeeCode().toLowerCase(Locale.ROOT).contains(searchQuery);
                if (matches) filtered.add(employee);
            }
            employeeAdapter.updateData(filtered);
            isEmpty = filtered.isEmpty();
        } else {
            joinRequestAdapter.updateData(new ArrayList<>(joinRequests));
            isEmpty = joinRequests.isEmpty();
        }

        binding.rvList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
