package com.example.attendanceapp.ui.employee.home;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentEmployeeHomeBinding;
import com.example.attendanceapp.domain.model.CategoryType;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.ui.employee.home.category.EmployeeCategoryTypeAdapter;
import com.example.attendanceapp.ui.employee.home.recent.EmployeeRecentAttendanceAdapter;
import com.example.attendanceapp.ui.employee.home.weekday.EmployeeWeekDayAdapter;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.core.utils.IntervalTicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeHomeFragment extends Fragment {

    // Tối ưu hóa pin: Giao diện tvClock chỉ hiển thị Giờ:Phút (HH:mm) nên tăng chu kỳ lên 60 giây để tránh lag UI và hao pin gánh chịu requestLayout liên tục
    private static final long CLOCK_TICK_INTERVAL_MS = 60000L;

    private FragmentEmployeeHomeBinding binding;
    private EmployeeHomeViewModel viewModel;
    private EmployeeCategoryTypeAdapter employeeCategoryTypeAdapter;
    private EmployeeWeekDayAdapter weekDayAdapter;
    private EmployeeRecentAttendanceAdapter employeeRecentAttendanceAdapter;

    private IntervalTicker clockTicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clockTicker = new IntervalTicker(CLOCK_TICK_INTERVAL_MS, this::updateClock);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EmployeeHomeViewModel.class);

        setupStaticViews();
        setupListeners();
        setupObservers();
        
        // Cập nhật đồng hồ hiển thị tức thì lần đầu tiên
        updateClock();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (clockTicker != null) clockTicker.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (clockTicker != null) clockTicker.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Giải phóng View Binding triệt để chống rò rỉ bộ nhớ RAM (Memory Leak)
    }

    private void setupObservers() {
        if (viewModel == null) return;
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;

            updateNotificationBadge(state.shouldShowNotificationBadge());
            renderTodayAttendanceCard(state);
            renderTodayShiftCard(state.getTodayShift());

            updateWeekDaysAdapter(state.getWeekDays());
            updateRecentAttendancesAdapter(state.getRecentAttendances());
        });
    }

    private void updateNotificationBadge(boolean visible) {
        if (binding != null && binding.includeHeader != null) {
            binding.includeHeader.notificationBadge.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void updateWeekDaysAdapter(List<WeekDay> weekDays) {
        if (weekDays != null && weekDayAdapter != null) {
            weekDayAdapter.submitList(weekDays);
        }
    }

    private void updateRecentAttendancesAdapter(List<RecentAttendance> recentAttendances) {
        if (binding == null || employeeRecentAttendanceAdapter == null) return;
        
        // Tối ưu hóa UI Trống (Empty State): Hiển thị hình ảnh minh họa thay vì khoảng trắng vô nghĩa nếu chưa có ngày công
        if (recentAttendances == null || recentAttendances.isEmpty()) {
            binding.includeRecent.rvRecent.setVisibility(View.GONE);
            binding.includeRecent.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.includeRecent.rvRecent.setVisibility(View.VISIBLE);
            binding.includeRecent.includeEmptyState.getRoot().setVisibility(View.GONE);
            employeeRecentAttendanceAdapter.submitList(recentAttendances);
        }
    }

    private void renderTodayAttendanceCard(EmployeeHomeUiState state) {
        if (binding == null) return;
        LocalTime checkInTime = state.getCheckInTime();
        LocalTime checkOutTime = state.getCheckOutTime();
        boolean hasShiftToday = state.getTodayShift() != null;

        if (hasShiftToday) {
            binding.includeTodayCard.cardToday.setVisibility(View.VISIBLE);
            binding.includeTodayCard.tvCheckInTime.setText(DateTimeFormatUtils.formatTime(checkInTime));
            binding.includeTodayCard.tvCheckOutTime.setText(DateTimeFormatUtils.formatTime(checkOutTime));
            binding.includeTodayCard.tvWorkedHours.setText(DateTimeFormatUtils.formatWorkedHours(checkInTime, checkOutTime));

            CheckState checkState = CheckState.from(checkInTime, checkOutTime);
            binding.includeTodayCard.btnCheckInCta.setText(checkState.getButtonText());
            binding.includeTodayCard.btnCheckInCta.setEnabled(checkState.isButtonEnabled());
        } else {
            binding.includeTodayCard.cardToday.setVisibility(View.GONE);
        }
    }

    private void renderTodayShiftCard(Shift todayShift) {
        if (binding == null) return;
        if (todayShift != null && todayShift.getDate().equals(LocalDate.now())) {
            binding.includeShiftToday.getRoot().setVisibility(View.VISIBLE);
            binding.includeShiftToday.tvShiftName.setText(todayShift.getShiftName());
            binding.includeShiftToday.tvShiftTime.setText(todayShift.getShiftTime());
            binding.includeShiftToday.tvShiftLocation.setText(todayShift.getLocation());

            bindShiftStatusChip(todayShift.getStatus());
        } else {
            binding.includeShiftToday.getRoot().setVisibility(View.GONE);
        }
    }

    private void bindShiftStatusChip(ShiftStatus status) {
        if (binding == null) return;
        Context context = getContext();
        if (context == null) return;

        ShiftStatus currentStatus = (status != null) ? status : ShiftStatus.PENDING;
        binding.includeShiftToday.tagShiftStatus.setText(currentStatus.getDisplayName());

        int bgColor = ColorUtil.getThemeColor(context, currentStatus.getBgAttrRes());
        int textColor = ColorUtil.getThemeColor(context, currentStatus.getTextColorAttrRes());

        binding.includeShiftToday.tagShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        binding.includeShiftToday.tagShiftStatus.setTextColor(textColor);
    }

    private void updateClock() {
        if (binding == null) return;
        // Đảm bảo cập nhật chữ trên Main Thread mượt mà an toàn không rò rỉ runnables
        binding.includeTodayCard.tvClock.setText(DateTimeFormatUtils.formatTime(LocalTime.now()));
    }

    private void setupStaticViews() {
        setupCategoryRecyclerView();
        setupWeekRecyclerView();
        setupRecentRecyclerView();
    }

    private void setupCategoryRecyclerView() {
        Context context = getContext();
        if (context == null) return;

        employeeCategoryTypeAdapter = new EmployeeCategoryTypeAdapter(this::onCategoryClicked);
        RecyclerView rv = binding.includeCategories.rvCategories;
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rv.setHasFixedSize(true);
        rv.setAdapter(employeeCategoryTypeAdapter);
        employeeCategoryTypeAdapter.submitList(Arrays.asList(CategoryType.values()));

        rv.post(this::updateScrollThumb);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateScrollThumb();
            }
        });
    }

    private void setupWeekRecyclerView() {
        Context context = getContext();
        if (context == null) return;
        binding.includeWeek.rvWeekDays.setLayoutManager(new GridLayoutManager(context, 7));
        binding.includeWeek.rvWeekDays.setHasFixedSize(true);
        weekDayAdapter = new EmployeeWeekDayAdapter();
        binding.includeWeek.rvWeekDays.setAdapter(weekDayAdapter);
    }

    private void setupRecentRecyclerView() {
        Context context = getContext();
        if (context == null) return;
        binding.includeRecent.rvRecent.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.includeRecent.rvRecent.setHasFixedSize(true);
        employeeRecentAttendanceAdapter = new EmployeeRecentAttendanceAdapter();
        binding.includeRecent.rvRecent.setAdapter(employeeRecentAttendanceAdapter);
    }

    private void setupListeners() {
        binding.includeHeader.btnNotification.setOnClickListener(v -> navigateTo(R.id.action_employeeHomeFragment_to_employeeNotificationFragment));
        binding.includeHeader.ivAvatar.setOnClickListener(v -> navigateTo(R.id.action_employeeHomeFragment_to_employeeProfileFragment));
        binding.includeTodayCard.btnCheckInCta.setOnClickListener(v -> navigateTo(R.id.action_employeeHomeFragment_to_employeeCheckInFragment));
        binding.includeShiftToday.tvExchangeShift.setOnClickListener(v -> navigateTo(R.id.action_employeeHomeFragment_to_employeeShiftExchangeFragment));
        binding.includeRecent.tvSeeAll.setOnClickListener(v -> navigateTo(R.id.action_employeeHomeFragment_to_employeeScheduleFragment));
    }

    private void onCategoryClicked(CategoryType categoryType) {
        if (categoryType == null) return;
        switch (categoryType) {
            case CHECK_IN: navigateTo(R.id.action_employeeHomeFragment_to_employeeCheckInFragment); break;
            case ADJUSTMENT: navigateTo(R.id.action_employeeHomeFragment_to_employeeAdjustmentFragment); break;
            case SWAP_SHIFT: navigateTo(R.id.action_employeeHomeFragment_to_employeeShiftExchangeFragment); break;
            case LEAVE_REQUEST: navigateTo(R.id.action_employeeHomeFragment_to_employeeLeaveHistoryFragment); break;
            case PAYROLL: navigateTo(R.id.action_employeeHomeFragment_to_employeePayrollFragment); break;
            case STATISTIC: navigateTo(R.id.action_employeeHomeFragment_to_employeeStatisticsFragment); break;
        }
    }

    private void navigateTo(int actionId) {
        if (!isAdded() || binding == null) return;
        NavController navController = NavHostFragment.findNavController(this);
        // Sửa lỗi Click đúp gây Crash: Chỉ thực thi lệnh điều hướng nếu người dùng đang đứng ở đúng màn hình Home chủ quản
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.employeeHomeFragment) {
            navController.navigate(actionId);
        }
    }

    private void updateScrollThumb() {
        if (binding == null) return;
        RecyclerView rv = binding.includeCategories.rvCategories;
        int totalRange = rv.computeHorizontalScrollRange();
        int extent = rv.computeHorizontalScrollExtent();
        int offset = rv.computeHorizontalScrollOffset();

        binding.includeCategories.scrollIndicatorTrack.setVisibility(View.VISIBLE);
        if (totalRange <= extent) {
            binding.includeCategories.scrollIndicatorThumb.setTranslationX(0f);
            return;
        }

        int trackWidth = binding.includeCategories.scrollIndicatorTrack.getWidth();
        int thumbWidth = binding.includeCategories.scrollIndicatorThumb.getWidth();
        float scrollPercent = (float) offset / (float) (totalRange - extent);
        binding.includeCategories.scrollIndicatorThumb.setTranslationX((trackWidth - thumbWidth) * scrollPercent);
    }
}