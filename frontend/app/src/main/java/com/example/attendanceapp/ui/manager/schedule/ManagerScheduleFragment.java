package com.example.attendanceapp.ui.manager.schedule;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.databinding.FragmentManagerScheduleBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerScheduleFragment extends Fragment {

    private FragmentManagerScheduleBinding binding;
    private ManagerScheduleViewModel viewModel;

    private final List<ManagerEmployeeItem> employees = new ArrayList<>();
    // Lưu ca đã xếp theo từng ngày: date -> (employeeId -> [tenCa, khungGio])
    private final Map<LocalDate, Map<String, String[]>> assignmentsByDate = new HashMap<>();

    private ManagerScheduleEmployeeAdapter adapter;
    private LocalDate selectedDate = LocalDate.now();
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerScheduleViewModel.class);

        binding.toolbarApp.setTitle("Xếp ca");

        loadMockData();
        setupWeekDaySelector();
        setupEmployeeList();
        setupSearch();
        setupSwipeRefresh();

        refreshEmployeeList();
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

        // Xếp sẵn vài ca cho hôm nay để minh họa
        Map<String, String[]> today = new HashMap<>();
        today.put("u1", new String[]{"Ca Sáng", "08:00 - 12:00"});
        today.put("u3", new String[]{"Ca Chiều", "13:00 - 17:00"});
        assignmentsByDate.put(LocalDate.now(), today);
    }

    private void setupWeekDaySelector() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(monday.plusDays(i));
        }

        ManagerWeekDaySelectorAdapter weekAdapter = new ManagerWeekDaySelectorAdapter(days, selectedDate, date -> {
            selectedDate = date;
            refreshEmployeeList();
        });

        binding.rvWeekDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.rvWeekDays.setAdapter(weekAdapter);
    }

    private void setupEmployeeList() {
        adapter = new ManagerScheduleEmployeeAdapter(new ArrayList<>(), (employee, position) ->
                openAssignShiftSheet(employee));
        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmployees.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                refreshEmployeeList();
            }

            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshEmployeeList();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void openAssignShiftSheet(ManagerEmployeeItem employee) {
        AssignShiftBottomSheet.newInstance(employee.getFullName(), selectedDate, (shiftName, shiftTime) -> {
            Map<String, String[]> dayMap = assignmentsByDate.computeIfAbsent(selectedDate, d -> new HashMap<>());
            if (shiftName == null) {
                dayMap.put(employee.getUserId(), new String[]{"Nghỉ", "Không có ca"});
                Toast.makeText(getContext(), "Đã bỏ xếp ca cho " + employee.getFullName(), Toast.LENGTH_SHORT).show();
            } else {
                dayMap.put(employee.getUserId(), new String[]{shiftName, shiftTime});
                Toast.makeText(getContext(), "Đã xếp " + shiftName + " cho " + employee.getFullName(), Toast.LENGTH_SHORT).show();
            }
            refreshEmployeeList();
        }).show(getChildFragmentManager(), "assign_shift");
    }

    private void refreshEmployeeList() {
        Map<String, String[]> dayMap = assignmentsByDate.getOrDefault(selectedDate, new HashMap<>());
        List<ManagerEmployeeItem> filtered = new ArrayList<>();

        for (ManagerEmployeeItem base : employees) {
            if (!searchQuery.isEmpty() && !base.getFullName().toLowerCase(Locale.ROOT).contains(searchQuery)) {
                continue;
            }
            String[] assigned = dayMap.get(base.getUserId());
            if (assigned != null) {
                base.setAssignedShiftName(assigned[0]);
                base.setAssignedShiftTime(assigned[1]);
            } else {
                base.setAssignedShiftName(null);
                base.setAssignedShiftTime(null);
            }
            filtered.add(base);
        }

        adapter.updateData(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.rvEmployees.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        String dayLabel = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
        binding.tvScheduleHint.setText("Xếp ca cho " + dayLabel + ", " +
                selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
