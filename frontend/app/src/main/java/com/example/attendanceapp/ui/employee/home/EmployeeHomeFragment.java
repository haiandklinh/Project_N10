package com.example.attendanceapp.ui.employee.home;

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

    private static final long CLOCK_TICK_INTERVAL_MS = 1000L;

    private FragmentEmployeeHomeBinding binding;
    private EmployeeHomeViewModel viewModel;
    private EmployeeCategoryTypeAdapter employeeCategoryTypeAdapter;
    private EmployeeWeekDayAdapter weekDayAdapter;
    private EmployeeRecentAttendanceAdapter employeeRecentAttendanceAdapter;

    private final IntervalTicker clockTicker = new IntervalTicker(CLOCK_TICK_INTERVAL_MS, this::updateClock);

    // =====================================================================
    // 1. LIFECYCLE MANAGEMENT
    // =====================================================================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel bằng Hilt / AndroidX ViewModelProvider
        viewModel = new ViewModelProvider(this).get(EmployeeHomeViewModel.class);

        setupStaticViews();
        setupListeners();
        setupObservers();
    }

    @Override
    public void onStart() {
        super.onStart();
        clockTicker.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        clockTicker.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Bắt buộc để giải phóng bộ nhớ tránh Memory Leak View Binding
    }

    // =====================================================================
    // 2. OBSERVERS (LẮNG NGHE UISTATE TỪ VIEWMODEL)
    // =====================================================================

    private void setupObservers() {
        // A. Đăng ký observer cho LiveData
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            // B. Null safety check
            if (state == null || binding == null) return;

            // C. Update từng phần UI
            updateNotificationBadge(state.shouldShowNotificationBadge());

                // Cards UI State
            renderTodayAttendanceCard(state);
            renderTodayShiftCard(state.getTodayShift());

                 // Lists UI State (Dùng submitList cho ListAdapter/DiffUtil)
            updateWeekDaysAdapter(state.getWeekDays());
            updateRecentAttendancesAdapter(state.getRecentAttendances());
        });
    }

    private void updateNotificationBadge(boolean visible) {
        binding.includeHeader.notificationBadge.setVisibility(
                visible ? View.VISIBLE : View.GONE
        );
    }

    private void updateWeekDaysAdapter(List<WeekDay> weekDays) {
        if (weekDays != null && weekDayAdapter != null) {
            // Update data cho adapter
            weekDayAdapter.submitList(weekDays);
        }
    }

    private void updateRecentAttendancesAdapter(List<RecentAttendance> recentAttendances) {
        if (recentAttendances != null && employeeRecentAttendanceAdapter != null) {
            employeeRecentAttendanceAdapter.submitList(recentAttendances);

            boolean isEmpty = recentAttendances.isEmpty();
            binding.includeRecent.rvRecent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.includeRecent.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    // =====================================================================
    // 3. RENDERERS (RENDER DỮ LIỆU TỪ STATE LÊN VIEW)
    // =====================================================================

    private void renderTodayAttendanceCard(EmployeeHomeUiState state) {
        // Lấy dữ liệu từ state
        LocalTime checkInTime = state.getCheckInTime();
        LocalTime checkOutTime = state.getCheckOutTime();
        boolean hasShiftToday = state.getTodayShift() != null;

        if (hasShiftToday) {
            binding.includeTodayCard.cardToday.setVisibility(View.VISIBLE);
            binding.includeTodayCard.tvCheckInTime.setText(DateTimeFormatUtils.formatTime(checkInTime));
            binding.includeTodayCard.tvCheckOutTime.setText(DateTimeFormatUtils.formatTime(checkOutTime));
            binding.includeTodayCard.tvWorkedHours.setText(DateTimeFormatUtils.formatWorkedHours(checkInTime, checkOutTime));

            // Cập nhật nút Check-in/Check-out trenj TodayCard
            CheckState checkState = CheckState.from(checkInTime, checkOutTime);
            binding.includeTodayCard.btnCheckInCta.setText(checkState.getButtonText());
            binding.includeTodayCard.btnCheckInCta.setEnabled(checkState.isButtonEnabled());
        } else {
            binding.includeTodayCard.cardToday.setVisibility(View.GONE);
        }
    }

    private void renderTodayShiftCard(Shift todayShift) {
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
        ShiftStatus currentStatus = (status != null) ? status : ShiftStatus.PENDING;
        binding.includeShiftToday.tagShiftStatus.setText(currentStatus.getDisplayName());

        int bgColor = ColorUtil.getThemeColor(requireContext(), currentStatus.getBgAttrRes());
        int textColor = ColorUtil.getThemeColor(requireContext(), currentStatus.getTextColorAttrRes());

        binding.includeShiftToday.tagShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        binding.includeShiftToday.tagShiftStatus.setTextColor(textColor);
    }

    private void updateClock() {
        if (binding == null) return;
        // Đảm bảo chạy cập nhật UI trên Main Thread
        binding.getRoot().post(() -> {
            if (binding != null) {
                binding.includeTodayCard.tvClock.setText(DateTimeFormatUtils.formatTime(LocalTime.now()));
            }
        });
    }

    // =====================================================================
    // 4. SETUP VIEWS & LISTENERS
    // =====================================================================

    private void setupStaticViews() {
        setupCategoryRecyclerView();
        setupWeekRecyclerView();
        setupRecentRecyclerView();
    }

    private void setupCategoryRecyclerView() {
        // Khởi tạo Adapter theo chuẩn ListAdapter + Consumer Click Callback
        employeeCategoryTypeAdapter = new EmployeeCategoryTypeAdapter(this::onCategoryClicked);

        RecyclerView rv = binding.includeCategories.rvCategories;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setHasFixedSize(true);
        rv.setAdapter(employeeCategoryTypeAdapter);

        // Đổ danh sách Enum cố định ban đầu bằng submitList
        employeeCategoryTypeAdapter.submitList(Arrays.asList(CategoryType.values()));

        // Scroll Indicator setup
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
        binding.includeWeek.rvWeekDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.includeWeek.rvWeekDays.setHasFixedSize(true);
        weekDayAdapter = new EmployeeWeekDayAdapter();
        binding.includeWeek.rvWeekDays.setAdapter(weekDayAdapter);
    }

    private void setupRecentRecyclerView() {
        binding.includeRecent.rvRecent.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
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

// =====================================================================
// 5. HELPER & NAVIGATION METHODS
// =====================================================================

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
        NavController navController = NavHostFragment.findNavController(this);
        // Safe check destination hiện tại để tránh Crash do click đúp (Double-click)
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