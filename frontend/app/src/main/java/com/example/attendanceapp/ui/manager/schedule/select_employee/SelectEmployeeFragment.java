package com.example.attendanceapp.ui.manager.schedule.select_employee;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentSelectEmployeeBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SelectEmployeeFragment extends Fragment {

    private FragmentSelectEmployeeBinding binding;
    private SelectEmployeeAdapter adapter;

    private final List<ManagerEmployeeItem> allEmployees = new ArrayList<>();
    private final Set<String> selectedEmployeeIds = new HashSet<>();

    private String searchQuery = "";
    private int selectedFilterStatus = FILTER_ALL;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_WORKING = 1;
    private static final int FILTER_ABSENT = 2;
    private static final int FILTER_LEAVE = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectEmployeeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadMockData();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupBottomBar();

        updateFilteredList();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Xếp ca nhân viên");
        binding.toolbarApp.setOnBackClickListener(() -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadMockData() {
        // Refactor: Sử dụng Enum EmployeeStatus.ACTIVE và EmployeeStatus.ON_LEAVE
        allEmployees.add(new ManagerEmployeeItem("u1", "NV001", "Nguyễn Văn A", null,
                "Nhân viên", "Cửa hàng Q1", "0901 111 222", "03/01/2025",
                EmployeeStatus.ACTIVE, 20, 1, 7200000));
        allEmployees.add(new ManagerEmployeeItem("u2", "NV002", "Trần Văn B", null,
                "Nhân viên", "Cửa hàng Q1", "0902 222 333", "12/03/2024",
                EmployeeStatus.ACTIVE, 22, 0, 7850000));
        allEmployees.add(new ManagerEmployeeItem("u3", "NV003", "Lê Thị C", null,
                "Thu ngân", "Cửa hàng Q1", "0903 333 444", "20/06/2025",
                EmployeeStatus.ACTIVE, 18, 2, 6500000));
        allEmployees.add(new ManagerEmployeeItem("u4", "NV004", "Phạm Văn D", null,
                "Nhân viên bảo vệ", "Cửa hàng Q3", "0904 444 555", "05/11/2023",
                EmployeeStatus.ACTIVE, 24, 0, 8100000));
        allEmployees.add(new ManagerEmployeeItem("u5", "NV005", "Hoàng Thị E", null,
                "Pha chế", "Cửa hàng Q1", "0905 555 666", "18/08/2025",
                EmployeeStatus.ACTIVE, 15, 3, 5200000));
        allEmployees.add(new ManagerEmployeeItem("u6", "NV006", "Đỗ Văn G", null,
                "Kho", "Kho vận", "0906 666 777", "10/02/2024",
                EmployeeStatus.ON_LEAVE, 12, 1, 6000000));

        // Mặc định chọn trước 3 nhân viên đầu tiên
        selectedEmployeeIds.add("u1");
        selectedEmployeeIds.add("u2");
        selectedEmployeeIds.add("u3");
    }

    private void setupRecyclerView() {
        adapter = new SelectEmployeeAdapter(new ArrayList<>(), selectedEmployeeIds, (employee, isChecked) -> {
            if (isChecked) {
                selectedEmployeeIds.add(employee.getUserId());
            } else {
                selectedEmployeeIds.remove(employee.getUserId());
            }
            updateBottomBarCount();
        });

        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmployees.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                updateFilteredList();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_ALL;
            updateFilteredList();
        });

        binding.chipWorking.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_WORKING;
            updateFilteredList();
        });

        binding.chipAbsent.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_ABSENT;
            updateFilteredList();
        });

        binding.chipLeave.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_LEAVE;
            updateFilteredList();
        });
    }

    private void updateFilteredList() {
        List<ManagerEmployeeItem> filtered = new ArrayList<>();

        for (ManagerEmployeeItem item : allEmployees) {
            // Lọc theo từ khóa tìm kiếm
            boolean matchesSearch = searchQuery.isEmpty() ||
                    item.getFullName().toLowerCase(Locale.ROOT).contains(searchQuery) ||
                    (item.getEmployeeCode() != null && item.getEmployeeCode().toLowerCase(Locale.ROOT).contains(searchQuery));

            // Lọc theo trạng thái Chip (So sánh Enum trực tiếp với ==)
            boolean matchesFilter = true;
            if (selectedFilterStatus == FILTER_WORKING) {
                matchesFilter = (item.getStatus() == EmployeeStatus.ACTIVE);
            } else if (selectedFilterStatus == FILTER_LEAVE) {
                matchesFilter = (item.getStatus() == EmployeeStatus.ON_LEAVE);
            } else if (selectedFilterStatus == FILTER_ABSENT) {
                matchesFilter = false; // Mock ví dụ Bỏ ca = 0
            }

            if (matchesSearch && matchesFilter) {
                filtered.add(item);
            }
        }

        adapter.updateData(filtered);
        updateBottomBarCount();
    }

    private void setupBottomBar() {
        updateBottomBarCount();

        binding.btnContinue.setOnClickListener(v -> {
            if (selectedEmployeeIds.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 nhân viên!", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.action_selectEmployeeFragment_to_assignShiftFragment);
        });
    }

    private void updateBottomBarCount() {
        int count = selectedEmployeeIds.size();
        binding.tvSelectedCount.setText("Đã chọn " + count + " nhân viên");
        binding.btnContinue.setEnabled(count > 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}