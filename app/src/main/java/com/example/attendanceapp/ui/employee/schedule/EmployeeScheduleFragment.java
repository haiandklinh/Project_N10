package com.example.attendanceapp.ui.employee.schedule;

import android.content.res.ColorStateList;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentEmployeeScheduleBinding;
import com.example.attendanceapp.databinding.LayoutScheduleTimelineBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.ui.employee.schedule.shift.EmployeeUpcomingShiftAdapter;
import com.example.attendanceapp.ui.employee.schedule.weekday.EmployeeScheduleWeekDayAdapter;
import com.example.attendanceapp.core.utils.ColorUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeScheduleFragment extends Fragment {

    private FragmentEmployeeScheduleBinding binding;
    private EmployeeScheduleWeekDayAdapter employeeScheduleWeekDayAdapter;
    private EmployeeUpcomingShiftAdapter shiftAdapter;

    private LocalDate selectedDate;
    private LocalDate currentWeekStart;

    public EmployeeScheduleFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedDate = LocalDate.now();
        currentWeekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.employeeScheduleFragment, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupWeekDaysRecyclerView();
        setupTodayShiftCard();
        setupUpcomingShiftRecyclerView();
        setupWeekNavigator();
        setupQuickActions();
        setupObservers();

        loadWeekData();
    }

    private void setupWeekDaysRecyclerView() {
        LayoutScheduleTimelineBinding timelineBinding = binding.includeCardTimeline;

        timelineBinding.rvWeekDays.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        employeeScheduleWeekDayAdapter = new EmployeeScheduleWeekDayAdapter(new ArrayList<>());
        timelineBinding.rvWeekDays.setAdapter(employeeScheduleWeekDayAdapter);

        employeeScheduleWeekDayAdapter.setOnDayClickListener(day -> {
            selectedDate = day.getDate();
            onDaySelected(selectedDate);
        });
    }

    // =====================================================================
    // BIND DỮ LIỆU CA LÀM HÔM NAY (dùng Shift model + ShiftStatus enum)
    // =====================================================================
    private void setupTodayShiftCard() {
        if (binding.includeShiftToday == null || binding.includeShiftToday.cardTodayShift == null) return;

        Shift todayShift = new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING
        );

        if (binding.includeShiftToday.tvShiftName != null) {
            binding.includeShiftToday.tvShiftName.setText(todayShift.getShiftName());
        }

        if (binding.includeShiftToday.tvShiftTime != null) {
            binding.includeShiftToday.tvShiftTime.setText(todayShift.getShiftTime());
        }

        if (binding.includeShiftToday.tvShiftLocation != null) {
            binding.includeShiftToday.tvShiftLocation.setText(todayShift.getLocation());
        }

        bindTodayShiftStatusChip(todayShift.getStatus());

        if (binding.includeShiftToday.tvExchangeShift != null) {
            binding.includeShiftToday.tvExchangeShift.setOnClickListener(v -> {
                Navigation.findNavController(v)
                        .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment);
            });
        }
    }

    private void bindTodayShiftStatusChip(ShiftStatus status) {
        if (binding.includeShiftToday.tagShiftStatus == null) return;

        ShiftStatus currentStatus = (status != null) ? status : ShiftStatus.PENDING;
        binding.includeShiftToday.tagShiftStatus.setText(currentStatus.getDisplayName());

        int bgAttr;
        int textColorAttr;

        switch (currentStatus) {
            case WORKING:
                bgAttr = R.attr.colorAttendancePresentContainer;
                textColorAttr = R.attr.colorAttendancePresent;
                break;

            case COMPLETED:
                bgAttr = R.attr.colorSurfaceVariant;
                textColorAttr = R.attr.colorOnSurfaceVariant;
                break;

            case PENDING:
            default:
                bgAttr = R.attr.colorAttendanceNotCheckedContainer;
                textColorAttr = R.attr.colorAttendanceNotChecked;
                break;
        }

        binding.includeShiftToday.tagShiftStatus.setChipBackgroundColor(
                ColorStateList.valueOf(ColorUtil.getThemeColor(requireContext(), bgAttr))
        );
        binding.includeShiftToday.tagShiftStatus.setTextColor(
                ColorUtil.getThemeColor(requireContext(), textColorAttr)
        );
    }

    // =====================================================================
    // DANH SÁCH CA LÀM SẮP TỚI
    // =====================================================================
    private void setupUpcomingShiftRecyclerView() {
        shiftAdapter = new EmployeeUpcomingShiftAdapter();

        binding.rvUpcomingShifts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingShifts.setNestedScrollingEnabled(false);
        binding.rvUpcomingShifts.setAdapter(shiftAdapter);

        List<Shift> list = new ArrayList<>();
        list.add(new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING));
        list.add(new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING)); list.add(new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING));
        shiftAdapter.submitList(list);
    }

    private void setupWeekNavigator() {
        LayoutScheduleTimelineBinding timelineBinding = binding.includeCardTimeline;

        timelineBinding.btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            loadWeekData();
        });

        timelineBinding.btnNextWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            loadWeekData();
        });
    }

    private void setupQuickActions() {
        binding.btnSwapShift.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment);
        });

        binding.btnRequestLeave.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_employeeScheduleFragment_to_employeeLeaveHistoryFragment);
        });
    }

    private void loadWeekData() {
        updateWeekRangeTitle();
        List<WeekDay> weekDays = createWeekDays();
        employeeScheduleWeekDayAdapter.submitList(weekDays);

        updateWeekSummary(5, "40.0 giờ");
    }

    private void updateWeekRangeTitle() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM", new Locale("vi", "VN"));

        String rangeText = currentWeekStart.format(formatter) + " – "
                + weekEnd.format(formatter) + ", "
                + currentWeekStart.getYear();

        binding.includeCardTimeline.tvWeekRange.setText(rangeText);
    }

    private List<WeekDay> createWeekDays() {
        List<WeekDay> days = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            boolean isToday = date.equals(today);
            AttendanceStatus status = resolveStatus(date);

            days.add(new WeekDay(date, status, isToday));
        }

        return days;
    }

    private AttendanceStatus resolveStatus(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return AttendanceStatus.WEEKEND;
        }
        return AttendanceStatus.NOT_CHECKED;
    }

    private void updateWeekSummary(int totalShifts, String totalHours) {
        LayoutScheduleTimelineBinding timelineBinding = binding.includeCardTimeline;
        timelineBinding.tvTotalShifts.setText(totalShifts + " ca");
        timelineBinding.tvTotalHours.setText(totalHours);
    }

    private void onDaySelected(LocalDate date) {
        Toast.makeText(requireContext(), "Ngày đã chọn: " + date.toString(), Toast.LENGTH_SHORT).show();
    }

    private void setupObservers() {
        // ViewModel Observers
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}