package com.example.attendanceapp.ui.manager.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentManagerScheduleBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.ui.manager.schedule.assign.AddShiftBottomSheet;
import com.example.attendanceapp.ui.manager.schedule.assign.AssignShiftBottomSheet;
import com.example.attendanceapp.ui.manager.schedule.assign.AssignShiftViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerScheduleFragment extends Fragment {
    @Inject
    SharedPreferenceApp sharedPreferenceApp;
    private FragmentManagerScheduleBinding binding;
    private ManagerScheduleViewModel viewModel;
    private AssignShiftViewModel assignViewModel;
    private ManagerScheduleEmployeeAdapter adapter;

    private ManagerEmployeeItem pendingEmployeeForAssign;

    // Dấu vết lần dựng chip gần nhất, để tránh dựng lại (làm thanh chip giật) khi chỉ đổi ngày
    private String lastChipSignature = null;

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
        assignViewModel = new ViewModelProvider(requireActivity()).get(AssignShiftViewModel.class);

        applyWindowInsets();
        setupWeekNavigation();
        setupEmployeeList();
        setupSwipeRefresh();
        setupFabAdd();
        setupBottomSheetResultListener();

        // Đồng bộ hiển thị ban đầu với chế độ đang được chọn trên toggle
        applyModeVisibility();

        setupObserves();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private boolean isMonthMode() {
        return binding.toggleGroupTime.getCheckedButtonId() == R.id.btn_mode_month;
    }

    /**
     * Chế độ Tuần: hiện strip 7 ngày. Chế độ Tháng: hiện lịch tháng.
     * Phần xếp ca (hint, nút Xếp ca, chip, danh sách nhân viên) luôn hiển thị ở cả hai chế độ.
     */
    private void applyModeVisibility() {
        boolean isMonth = isMonthMode();
        binding.rvWeekDays.setVisibility(isMonth ? View.GONE : View.VISIBLE);
        binding.layoutCalendarMonth.getRoot().setVisibility(isMonth ? View.VISIBLE : View.GONE);
    }

    private void setupObserves() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.swipeRefreshLayout.setRefreshing(state.isLoading());

            if (state.getError() != null) {
                Toast.makeText(getContext(), "Lỗi: " + state.getError(), Toast.LENGTH_SHORT).show();
            }

            if (state.isAssignSuccess()) {
                Toast.makeText(getContext(), "Xếp ca thành công", Toast.LENGTH_SHORT).show();
            }

            // Render UI
            updateEmployeeList(state.getEmployees(), state.getShiftOptions());
            updateShiftChips(state.getShiftOptions());

            if (isMonthMode()) {
                updateMonthCalendar(state);
            } else {
                updateWeekDaySelector(state);
            }
        });

        // uiState đã phát lại mỗi khi selectedDate đổi, nên ở đây chỉ cập nhật tiêu đề
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> updateTitleText());

        // Observe isPastDate để disable các action khi chọn ngày quá khứ
        viewModel.getIsPastDate().observe(getViewLifecycleOwner(), isPast -> {
            binding.btnAddShift.setEnabled(!isPast);
            binding.btnAddShift.setAlpha(isPast ? 0.5f : 1.0f);
            binding.cardPastDateWarning.setVisibility(isPast ? View.VISIBLE : View.GONE);
        });
    }

    private void updateEmployeeList(List<ManagerEmployeeItem> employees, List<ShiftOption> shiftOptions) {
        adapter.updateData(employees);

        boolean isListEmpty = employees.isEmpty();

        binding.rvEmployees.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.getRoot().setVisibility(isListEmpty ? View.VISIBLE : View.GONE);

        if (isListEmpty) {
            String filter = viewModel.getSelectedShiftFilter();
            if (ManagerScheduleViewModel.FILTER_UNASSIGNED.equals(filter)) {
                binding.layoutEmptyState.tvEmptySubtitle.setText("Tất cả nhân viên đã được xếp ca");
            } else if (filter != null) {
                String shiftTitle = filter;
                if (shiftOptions != null) {
                    for (ShiftOption option : shiftOptions) {
                        if (filter.equals(option.getId())) {
                            shiftTitle = option.getTitle();
                            break;
                        }
                    }
                }
                binding.layoutEmptyState.tvEmptySubtitle.setText("Không có nhân viên nào làm " + shiftTitle);
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

        // Bấm ngày chỉ chọn ngày, vẫn ở chế độ tháng
        ManagerMonthCalendarAdapter monthAdapter = new ManagerMonthCalendarAdapter(
                gridDays, selectedDate, selectedDate,
                date -> viewModel.setSelectedDate(date)
        );
        monthAdapter.setDayStatuses(state.getDayStatuses());

        binding.layoutCalendarMonth.rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.layoutCalendarMonth.rvCalendarGrid.setAdapter(monthAdapter);
    }

    private void setupWeekNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> viewModel.prevWeekOrMonth(!isMonthMode()));
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextWeekOrMonth(!isMonthMode()));

        binding.toggleGroupTime.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            // Chỉ đổi giữa strip 7 ngày và lịch tháng
            applyModeVisibility();

            ManagerScheduleUiState state = viewModel.getUiState().getValue();
            if (state != null) {
                if (isMonthMode()) {
                    updateMonthCalendar(state);
                } else {
                    updateWeekDaySelector(state);
                }
            }

            updateTitleText();
        });
    }

    private void updateTitleText() {
        LocalDate selectedDate = viewModel.getSelectedDate().getValue();
        LocalDate monday = viewModel.getCurrentWeekMonday().getValue();
        if (selectedDate == null || monday == null) return;

        if (!isMonthMode()) {
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
        binding.tvScheduleHint.setText(
                selectedDate.equals(LocalDate.now())
                        ? "Hôm nay"
                        : dayOfWeekName + ", " + dateFormatted
        );
    }

    private void setupEmployeeList() {
        adapter = new ManagerScheduleEmployeeAdapter(new ArrayList<>(), (employee, position) -> {
            Boolean isPast = viewModel.getIsPastDate().getValue();
            if (Boolean.TRUE.equals(isPast)) {
                return;
            }
            LocalDate selectedDate = viewModel.getSelectedDate().getValue();
            if (selectedDate == null) selectedDate = LocalDate.now();
            openQuickAssignBottomSheet(employee, selectedDate);
        });
        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmployees.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshData());
    }

    private void setupFabAdd() {
        binding.btnAddShift.setOnClickListener(v -> {
            assignViewModel.startNewFlow(viewModel.getSelectedDate().getValue());
            Navigation.findNavController(v).navigate(R.id.action_managerScheduleFragment_to_selectEmployee);
        });
    }

    private void setupBottomSheetResultListener() {
        // Lắng nghe xếp ca từ AssignShiftBottomSheet
        getChildFragmentManager().setFragmentResultListener(
                AssignShiftBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    ShiftOption selectedShift = result.getParcelable(AssignShiftBottomSheet.EXTRA_SELECTED_SHIFT);
                    LocalDate selectedDate = viewModel.getSelectedDate().getValue();
                    if (selectedDate == null) selectedDate = LocalDate.now();

                    if (pendingEmployeeForAssign != null) {
                        if (selectedShift != null && sharedPreferenceApp != null) {
                            if (selectedShift.getLatitude() != null && selectedShift.getLongitude() != null
                                    && (selectedShift.getLatitude() != 0.0 || selectedShift.getLongitude() != 0.0)) {
                                double radius = selectedShift.getCheckInRadius() != null && selectedShift.getCheckInRadius() > 0 ? selectedShift.getCheckInRadius() : sharedPreferenceApp.getShiftRadius(selectedShift.getTitle());
                                if (radius <= 0) radius = sharedPreferenceApp.getLatestShiftRadius();
                                if (radius <= 0) radius = 100.0;
                                sharedPreferenceApp.saveShiftLocation(
                                        selectedShift.getTitle(),
                                        selectedShift.getLatitude(),
                                        selectedShift.getLongitude(),
                                        radius
                                );
                                sharedPreferenceApp.saveLatestShiftLocation(
                                        selectedShift.getLatitude(),
                                        selectedShift.getLongitude(),
                                        radius,
                                        ""
                                );
                            }
                        }
                        viewModel.assignWorkSchedule(
                                pendingEmployeeForAssign.getUserId(),
                                selectedShift != null ? selectedShift.getId() : null,
                                selectedDate.toString()
                        );
                        pendingEmployeeForAssign = null;
                    }
                }
        );

        // Lắng nghe Chỉnh sửa ca từ AddShiftBottomSheet (mở từ Menu Long Click Chip)
        getChildFragmentManager().setFragmentResultListener(
                AddShiftBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    ShiftOption resultShift = bundle.getParcelable(AddShiftBottomSheet.EXTRA_RESULT_SHIFT);
                    if (resultShift != null && resultShift.getId() != null) {
                        if (sharedPreferenceApp != null && resultShift.getLatitude() != null && resultShift.getLongitude() != null) {
                            sharedPreferenceApp.saveShiftLocation(
                                    resultShift.getTitle(),
                                    resultShift.getLatitude(),
                                    resultShift.getLongitude(),
                                    resultShift.getCheckInRadius() != null ? resultShift.getCheckInRadius() : 100.0
                            );
                            sharedPreferenceApp.saveLatestShiftLocation(
                                    resultShift.getLatitude(),
                                    resultShift.getLongitude(),
                                    resultShift.getCheckInRadius() != null ? resultShift.getCheckInRadius() : 100.0,
                                    ""
                            );
                        }
                        viewModel.updateShift(
                                resultShift.getId(),
                                resultShift.getTitle(),
                                resultShift.getStartTime(),
                                resultShift.getEndTime(),
                                resultShift.getAllowedCheckInTime(),
                                resultShift.getCheckInRadius() != null ? resultShift.getCheckInRadius() : 100.0,
                                resultShift.getLatitude(),
                                resultShift.getLongitude()
                        );
                        Toast.makeText(getContext(), "Đã cập nhật " + resultShift.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openQuickAssignBottomSheet(ManagerEmployeeItem employee, LocalDate date) {
        this.pendingEmployeeForAssign = employee;
        List<ShiftOption> currentShifts = viewModel.getUiState().getValue() != null
                ? viewModel.getUiState().getValue().getShiftOptions()
                : new ArrayList<>();

        AssignShiftBottomSheet bottomSheet = AssignShiftBottomSheet.newInstance(
                employee.getFullName(),
                date,
                currentShifts
        );

        bottomSheet.show(getChildFragmentManager(), "quick_assign_shift");
    }

    private void updateShiftChips(List<ShiftOption> shiftOptions) {
        // Chỉ dựng lại chip khi danh sách ca hoặc bộ lọc thật sự đổi
        String signature = buildChipSignature(shiftOptions);
        if (signature.equals(lastChipSignature)) return;
        lastChipSignature = signature;

        binding.chipGroupShifts.removeAllViews();

        // 1. Chip "Tất cả"
        addFilterChip("Tất cả", null, null);

        // 2. Chip các ca làm việc động
        if (shiftOptions != null) {
            for (ShiftOption option : shiftOptions) {
                String label = option.getTitle();
                String timeRange = option.getTimeRange();
                if (timeRange != null && !timeRange.isEmpty()) {
                    label += " (" + timeRange + ")";
                }
                addFilterChip(label, option.getId(), option);
            }
        }

        // 3. Chip "Chưa xếp"
        addFilterChip("Chưa xếp", ManagerScheduleViewModel.FILTER_UNASSIGNED, null);
    }

    private String buildChipSignature(List<ShiftOption> shiftOptions) {
        StringBuilder sb = new StringBuilder();
        sb.append(viewModel.getSelectedShiftFilter()).append('|');
        if (shiftOptions != null) {
            for (ShiftOption o : shiftOptions) {
                sb.append(o.getId()).append(':')
                        .append(o.getTitle()).append(':')
                        .append(o.getTimeRange()).append(';');
            }
        }
        return sb.toString();
    }

    private void addFilterChip(String label, String filterValue, ShiftOption shiftOption) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, binding.chipGroupShifts, false);
        chip.setText(label);
        chip.setId(View.generateViewId());

        String currentFilter = viewModel.getSelectedShiftFilter();
        boolean isSelected = (currentFilter == null && filterValue == null)
                || (currentFilter != null && currentFilter.equals(filterValue));

        chip.setOnCheckedChangeListener(null);
        chip.setChecked(isSelected);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setFilter(filterValue);
            }
        });

        // Bắt sự kiện ấn giữ (Long Click) để chỉnh sửa hoặc xóa ca
        // Chỉ cho phép khi không phải ngày quá khứ
        if (shiftOption != null) {
            Boolean isPast = viewModel.getIsPastDate().getValue();
            if (isPast == null || !isPast) {
                chip.setOnLongClickListener(v -> {
                    showShiftActionMenu(v, shiftOption);
                    return true;
                });
            }
        }

        binding.chipGroupShifts.addView(chip);
    }

    private void showShiftActionMenu(View anchorView, ShiftOption shift) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.getMenu().add(0, 1, 0, "Chỉnh sửa ca");
        popup.getMenu().add(0, 2, 1, "Xóa ca");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                AddShiftBottomSheet bottomSheet = AddShiftBottomSheet.newInstanceForEdit(shift);
                bottomSheet.show(getChildFragmentManager(), "edit_shift_from_chip");
                return true;
            } else if (item.getItemId() == 2) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa ca làm việc")
                        .setMessage("Bạn có chắc chắn muốn xóa " + shift.getTitle() + " không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            viewModel.deleteShift(shift);
                            Toast.makeText(getContext(), "Đã xóa ca " + shift.getTitle(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastChipSignature = null;
        binding = null;
    }
}