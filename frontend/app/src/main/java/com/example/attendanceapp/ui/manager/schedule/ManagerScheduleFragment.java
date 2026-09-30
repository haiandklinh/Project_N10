package com.example.attendanceapp.ui.manager.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerScheduleBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.ui.manager.schedule.assign.AssignShiftBottomSheet;
import com.google.android.material.chip.Chip;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerScheduleFragment extends Fragment {

    private FragmentManagerScheduleBinding binding;
    private ManagerScheduleViewModel viewModel;
    private ManagerScheduleEmployeeAdapter adapter;

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

        applyWindowInsets();
        setupWeekNavigation();
        setupEmployeeList();
        setupSwipeRefresh();
        setupFabAdd();

        observeViewModel();

        viewModel.loadEmployeeWorkSchedules();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.swipeRefreshLayout.setRefreshing(state.isLoading());

            if (state.getError() != null) {
                Toast.makeText(getContext(), "Lỗi: " + state.getError(), Toast.LENGTH_SHORT).show();
            }

            if (state.isAssignSuccess()) {
                Toast.makeText(getContext(), "Xếp ca thành công", Toast.LENGTH_SHORT).show();
            }

            // Render UI
            updateEmployeeList(state.getEmployees());
            updateShiftChips(state.getShiftOptions());
            updateWeekDaySelector(state);

            if (binding.toggleGroupTime.getCheckedButtonId() == R.id.btn_mode_month) {
                updateMonthCalendar(state);
            }
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> updateTitleText());
    }

    private void updateEmployeeList(List<ManagerEmployeeItem> employees) {
        adapter.updateData(employees);

        boolean isListEmpty = employees.isEmpty();
        binding.rvEmployees.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.getRoot().setVisibility(isListEmpty ? View.VISIBLE : View.GONE);

        if (isListEmpty) {
            String filter = viewModel.getSelectedShiftFilter();
            if ("UNASSIGNED".equals(filter)) {
                binding.layoutEmptyState.tvEmptySubtitle.setText("Tất cả nhân viên đã được xếp ca");
            } else if (filter != null) {
                binding.layoutEmptyState.tvEmptySubtitle.setText("Không có nhân viên nào làm " + filter);
            } else {
                binding.layoutEmptyState.tvEmptySubtitle.setText("Chưa có nhân viên nào trong danh sách");
            }
        }
    }

    private void updateWeekDaySelector(ManagerScheduleUiState state) {
        LocalDate monday = viewModel.getCurrentWeekMonday().getValue();
        LocalDate selectedDate = viewModel.getSelectedDate().getValue();
        if (monday == null || selectedDate == null) return;

        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(monday.plusDays(i));
        }

        ManagerWeekDaySelectorAdapter weekAdapter = new ManagerWeekDaySelectorAdapter(days, selectedDate, date -> {
            viewModel.setSelectedDate(date);
        });
        weekAdapter.setDayStatuses(state.getDayStatuses());

        binding.rvWeekDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.rvWeekDays.setAdapter(weekAdapter);
    }

    private void updateMonthCalendar(ManagerScheduleUiState state) {
        LocalDate selectedDate = viewModel.getSelectedDate().getValue();
        if (selectedDate == null) return;

        LocalDate firstDayOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeekOfFirst = firstDayOfMonth.getDayOfWeek().getValue();
        LocalDate gridStartDate = firstDayOfMonth.minusDays(dayOfWeekOfFirst - 1);

        List<LocalDate> gridDays = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            gridDays.add(gridStartDate.plusDays(i));
        }

        ManagerMonthCalendarAdapter monthAdapter = new ManagerMonthCalendarAdapter(
                gridDays, selectedDate, selectedDate,
                date -> {
                    viewModel.setSelectedDate(date);
                    binding.toggleGroupTime.check(R.id.btn_mode_week);
                }
        );
        monthAdapter.setDayStatuses(state.getDayStatuses());

        binding.layoutCalendarMonth.rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.layoutCalendarMonth.rvCalendarGrid.setAdapter(monthAdapter);
    }

    private void setupWeekNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            boolean isWeek = binding.toggleGroupTime.getCheckedButtonId() == R.id.btn_mode_week;
            viewModel.prevWeekOrMonth(isWeek);
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            boolean isWeek = binding.toggleGroupTime.getCheckedButtonId() == R.id.btn_mode_week;
            viewModel.nextWeekOrMonth(isWeek);
        });

        binding.toggleGroupTime.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean isWeek = checkedId == R.id.btn_mode_week;
                binding.rvWeekDays.setVisibility(isWeek ? View.VISIBLE : View.GONE);
                binding.layoutCalendarMonth.layoutCalendarRoot.setVisibility(isWeek ? View.GONE : View.VISIBLE);

                binding.tvScheduleHint.setVisibility(isWeek ? View.VISIBLE : View.GONE);
                binding.btnAddShift.setVisibility(isWeek ? View.VISIBLE : View.GONE);
                binding.divider1.setVisibility(isWeek ? View.VISIBLE : View.GONE);
                binding.scrollViewChips.setVisibility(isWeek ? View.VISIBLE : View.GONE);
                binding.swipeRefreshLayout.setVisibility(isWeek ? View.VISIBLE : View.GONE);

                updateTitleText();
            }
        });
    }

    private void updateTitleText() {
        LocalDate selectedDate = viewModel.getSelectedDate().getValue();
        LocalDate monday = viewModel.getCurrentWeekMonday().getValue();
        if (selectedDate == null || monday == null) return;

        if (binding.toggleGroupTime.getCheckedButtonId() == R.id.btn_mode_week) {
            LocalDate sunday = monday.plusDays(6);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            String rangeText = monday.format(fmt) + " – " + sunday.format(fmt) + "/" + monday.getYear();
            binding.tvMonthTitle.setText(rangeText);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("'Tháng' MM, yyyy", new Locale("vi"));
            binding.tvMonthTitle.setText(selectedDate.format(fmt));
        }

        String dayOfWeekName = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
        String dateFormatted = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        binding.tvScheduleHint.setText((selectedDate.equals(LocalDate.now()) ? "Hôm nay - " : "") + dayOfWeekName + ", " + dateFormatted);
    }

    private void setupEmployeeList() {
        adapter = new ManagerScheduleEmployeeAdapter(new ArrayList<>(), (employee, position) -> {
            LocalDate selectedDate = viewModel.getSelectedDate().getValue();
            if (selectedDate == null) selectedDate = LocalDate.now();
            openQuickAssignBottomSheet(employee, selectedDate);
        });
        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmployees.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadEmployeeWorkSchedules());
    }

    private void setupFabAdd() {
        binding.btnAddShift.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_managerScheduleFragment_to_selectEmployee)
        );
    }

    private void openQuickAssignBottomSheet(ManagerEmployeeItem employee, LocalDate date) {
        List<ShiftOption> currentShifts = viewModel.getUiState().getValue() != null
                ? viewModel.getUiState().getValue().getShiftOptions()
                : new ArrayList<>();

        AssignShiftBottomSheet bottomSheet = AssignShiftBottomSheet.newInstance(
                employee.getFullName(),
                date,
                currentShifts,
                selectedShift -> {
                    if (selectedShift != null) {
                        viewModel.assignWorkSchedule(
                                employee.getUserId(),
                                employee.getFullName(),
                                employee.getPosition(),
                                selectedShift.getId(),
                                date.toString()
                        );
                    } else {
                        // Xử lý hủy ca / cho nghỉ nếu cần
                    }
                }
        );

        bottomSheet.show(getChildFragmentManager(), "quick_assign_shift");
    }

    private void updateShiftChips(List<ShiftOption> shiftOptions) {
        binding.chipGroupShifts.removeAllViews();
        addFilterChip("Tất cả", null);

        if (shiftOptions != null) {
            for (ShiftOption option : shiftOptions) {
                String label = option.getTitle();
                String timeRange = option.getTimeRange();
                if (timeRange != null && !timeRange.isEmpty()) {
                    label += " (" + timeRange + ")";
                }
                addFilterChip(label, option.getTitle());
            }
        }

        addFilterChip("Chưa xếp", "UNASSIGNED");
    }

    private void addFilterChip(String label, String filterValue) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, binding.chipGroupShifts, false);
        chip.setText(label);
        chip.setId(View.generateViewId());

        String currentFilter = viewModel.getSelectedShiftFilter();
        boolean isSelected = (currentFilter == null && filterValue == null)
                || (currentFilter != null && currentFilter.equals(filterValue));

        chip.setChecked(isSelected);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setFilter(filterValue);
            }
        });

        binding.chipGroupShifts.addView(chip);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}