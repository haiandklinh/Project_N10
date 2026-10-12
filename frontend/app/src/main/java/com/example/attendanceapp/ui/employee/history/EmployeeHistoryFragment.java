package com.example.attendanceapp.ui.employee.history;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentEmployeeHistoryBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.HistoryAttendance;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.ui.employee.history.calendar.EmployeeCalendarDayAdapter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeHistoryFragment extends Fragment {

    private FragmentEmployeeHistoryBinding binding;
    private EmployeeHistoryAttendanceAdapter employeeHistoryAttendanceAdapter;
    private EmployeeCalendarDayAdapter employeeCalendarDayAdapter;

    private YearMonth currentMonth;
    @Nullable
    private AttendanceStatus currentFilter = null; // null đại diện cho Filter ALL (Tất cả)

    private final List<HistoryAttendance> allAttendances = new ArrayList<>();

    public EmployeeHistoryFragment() {
        // Constructor rỗng mặc định BẮT BUỘC cho Fragment
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentMonth = YearMonth.now();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.includeHeader.headerSection, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupViews();
        setupListeners();
        setupObservers();

        updateMonthTitle();
        loadDataForCurrentMonth();
    }

    private void setupViews() {
        // 1. Setup RecyclerView Danh sách
        employeeHistoryAttendanceAdapter = new EmployeeHistoryAttendanceAdapter(new ArrayList<>());
        binding.includeLayoutRv.rvHistory.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false));
        binding.includeLayoutRv.rvHistory.setAdapter(employeeHistoryAttendanceAdapter);
        binding.includeLayoutRv.rvHistory.setHasFixedSize(true);

        // 2. Setup RecyclerView Lịch (GridLayout 7 cột)
        employeeCalendarDayAdapter = new EmployeeCalendarDayAdapter(new ArrayList<>());
        binding.includeCalendar.rvCalendarGrid.setLayoutManager(new GridLayoutManager(
                requireContext(),
                7
        ));
        binding.includeCalendar.rvCalendarGrid.setAdapter(employeeCalendarDayAdapter);
    }

    private void setupListeners() {

        // Toggle chế độ Danh sách / Lịch
        binding.includeHeader.toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_view_list) {
                    binding.includeLayoutRv.layoutListView.setVisibility(View.VISIBLE);
                    binding.includeCalendar.cardCalendar.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_view_calendar) {
                    binding.includeLayoutRv.layoutListView.setVisibility(View.GONE);
                    binding.includeCalendar.cardCalendar.setVisibility(View.VISIBLE);
                }
            }
        });

        // Chuyển tháng
        binding.includeHeader.btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthTitle();
            loadDataForCurrentMonth();
        });

        binding.includeHeader.btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthTitle();
            loadDataForCurrentMonth();
        });

        // Click ngày trên lịch
        employeeCalendarDayAdapter.setOnDayClickListener(day -> {
            Toast.makeText(requireContext(),
                    "Ngày: " + day.getDate() + " - " + day.getStatus().getDisplayName(),
                    Toast.LENGTH_SHORT).show();
        });

        // Lọc theo ChipGroup
        binding.includeLayoutRv.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_present) {
                currentFilter = AttendanceStatus.PRESENT;
            } else if (checkedId == R.id.chip_late) {
                currentFilter = AttendanceStatus.LATE;
            } else if (checkedId == R.id.chip_absent) {
                currentFilter = AttendanceStatus.ABSENT;
            } else if (checkedId == R.id.chip_early) {
                currentFilter = AttendanceStatus.EARLY_LEAVE;
            } else {
                currentFilter = null; // Tất cả
            }

            applyFilterAndUpdateList();
        });
    }

    private void loadDataForCurrentMonth() {
        allAttendances.clear();
        allAttendances.addAll(getSampleAttendances());

        updateSummaryStats();
        applyFilterAndUpdateList();
        updateCalendarGrid();
    }

    private void updateCalendarGrid() {
        List<WeekDay> monthDays = createMonthDays(currentMonth);
        employeeCalendarDayAdapter.submitList(monthDays);
    }

    private List<WeekDay> createMonthDays(YearMonth yearMonth) {
        List<WeekDay> days = new ArrayList<>();
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();
        LocalDate today = LocalDate.now();

        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int emptyDaysBefore = dayOfWeek - 1;

        for (int i = 0; i < emptyDaysBefore; i++) {
            days.add(new WeekDay(
                    firstOfMonth.minusDays(emptyDaysBefore - i),
                    AttendanceStatus.WEEKEND,
                    false
            ));
        }

        for (int day = 1; day <= lastOfMonth.getDayOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            AttendanceStatus status = getAttendanceStatusForDate(date);
            boolean isToday = date.equals(today);

            days.add(new WeekDay(date, status, isToday));
        }

        return days;
    }

    private AttendanceStatus getAttendanceStatusForDate(LocalDate date) {
        for (HistoryAttendance attendance : allAttendances) {
            if (attendance.getDate().equals(date)) {
                return attendance.getStatus();
            }
        }
        return AttendanceStatus.NOT_CHECKED;
    }

    private List<HistoryAttendance> getSampleAttendances() {
        LocalDate today = LocalDate.now();

        return Arrays.asList(
                new HistoryAttendance(
                        today.withDayOfMonth(1),
                        LocalTime.of(7, 58),
                        LocalTime.of(17, 30),
                        AttendanceStatus.PRESENT,
                        "Văn phòng HCM"),
                new HistoryAttendance(
                        today.withDayOfMonth(2),
                        LocalTime.of(8, 15),
                        LocalTime.of(17, 30),
                        AttendanceStatus.LATE,
                        "Văn phòng HCM"),
                new HistoryAttendance(
                        today.withDayOfMonth(3),
                        LocalTime.of(8, 0),
                        LocalTime.of(16, 30),
                        AttendanceStatus.EARLY_LEAVE,
                        "Văn phòng Hà Nội"),
                new HistoryAttendance(
                        today.withDayOfMonth(4),
                        null,
                        null,
                        AttendanceStatus.ABSENT,
                        null),
                new HistoryAttendance(
                        today.withDayOfMonth(5),
                        null,
                        null,
                        AttendanceStatus.LEAVE,
                        null)
        );
    }

    private void applyFilterAndUpdateList() {
        List<HistoryAttendance> filtered;

        if (currentFilter == null) {
            filtered = new ArrayList<>(allAttendances);
        } else {
            filtered = allAttendances.stream()
                    .filter(item -> item.getStatus() == currentFilter)
                    .collect(Collectors.toList());
        }

        employeeHistoryAttendanceAdapter.updateData(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.includeLayoutRv.rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includeLayoutRv.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateSummaryStats() {
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int earlyLeaveCount = 0;
        long totalWorkingMinutes = 0;

        for (HistoryAttendance item : allAttendances) {
            if (item.getStatus() == AttendanceStatus.PRESENT) {
                presentCount++;
            } else if (item.getStatus() == AttendanceStatus.LATE) {
                lateCount++;
            } else if (item.getStatus() == AttendanceStatus.ABSENT) {
                absentCount++;
            } else if (item.getStatus() == AttendanceStatus.EARLY_LEAVE) {
                earlyLeaveCount++;
            }

            // Tính tổng thời gian làm việc cho các ngày có check-in và check-out thực tế
            if (item.getCheckIn() != null && item.getCheckOut() != null) {
                Duration duration = Duration.between(item.getCheckIn(), item.getCheckOut());
                if (!duration.isNegative()) {
                    totalWorkingMinutes += duration.toMinutes();
                }
            }
        }

        binding.includeHeader.tvOnTimeCount.setText(String.valueOf(presentCount));
        binding.includeHeader.tvLateCount.setText(String.valueOf(lateCount));
        binding.includeHeader.tvAbsCount.setText(String.valueOf(absentCount));
        binding.includeHeader.tvEarlyLeaveCount.setText(String.valueOf(earlyLeaveCount));

        double totalHours = totalWorkingMinutes / 60.0;
        binding.includeHeader.tvTotalWorkingHours.setText(String.format(Locale.US, "%.1f giờ", totalHours));
    }

    private void updateMonthTitle() {
        String monthName = currentMonth
                .getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));
        int year = currentMonth.getYear();
        String formattedTitle = capitalizeFirstLetter(monthName) + " " + year;

        binding.includeHeader.tvMonthYearSubtitle.setText(formattedTitle);
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + text.substring(1);
    }

    private void setupObservers() {
        // Observers cho LiveData/StateFlow từ ViewModel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}