package com.example.attendanceapp.ui.employee.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.attendanceapp.R;
import com.example.attendanceapp.core.notification.ShiftAlarmScheduler;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.core.utils.IntervalTicker;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeHomeFragment extends Fragment {

    private static final long CLOCK_TICK_INTERVAL_MS = 1000L;

    private FragmentEmployeeHomeBinding binding;
    private EmployeeHomeViewModel viewModel;
    private EmployeeCategoryTypeAdapter employeeCategoryTypeAdapter;
    private EmployeeWeekDayAdapter weekDayAdapter;
    private EmployeeRecentAttendanceAdapter employeeRecentAttendanceAdapter;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

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
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        clockTicker.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // =====================================================================
    // 2. OBSERVERS (LẮNG NGHE UISTATE TỪ VIEWMODEL)
    // =====================================================================

    private void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;

            updateHeader(state);
            updateNotificationBadge(state.shouldShowNotificationBadge());

            // Render các thành phần giao diện chính (luôn hiển thị theo yêu cầu)
            renderTodayAttendanceCard(state);
            renderTodayShiftCard(state.getTodayShift());

            updateWeekDaysAdapter(state.getWeekDays());
            updateRecentAttendancesAdapter(state.getRecentAttendances());
        });
    }

    private void updateNotificationBadge(boolean visible) {
        if (binding == null) return;
        binding.includeHeader.notificationBadge.setVisibility(
                visible ? View.VISIBLE : View.GONE
        );
    }

    private void updateHeader(EmployeeHomeUiState state) {
        if (binding == null) return;
        binding.includeHeader.tvGreeting.setText(state.getUserName());
        binding.includeHeader.tvCompanyName.setText(state.getCompanyName());

        Glide.with(this)
                .load(state.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.includeHeader.ivAvatar);
    }

    private void updateWeekDaysAdapter(List<WeekDay> weekDays) {
        if (weekDays != null && weekDayAdapter != null && binding != null) {
            // Luôn hiển thị layout_home_week
            binding.includeWeek.getRoot().setVisibility(View.VISIBLE);
            weekDayAdapter.submitList(weekDays);
        }
    }

    private void updateRecentAttendancesAdapter(List<RecentAttendance> recentAttendances) {
        if (recentAttendances != null && employeeRecentAttendanceAdapter != null && binding != null) {
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
        if (binding == null) return;

        LocalTime checkInTime = state.getCheckInTime();
        LocalTime checkOutTime = state.getCheckOutTime();
        boolean hasShiftToday = state.getTodayShift() != null;

        // Luôn hiển thị layout_home_today_card (card_today)
        binding.includeTodayCard.cardToday.setVisibility(View.VISIBLE);

        if (hasShiftToday) {
            binding.includeTodayCard.tvCheckInTime.setText(DateTimeFormatUtils.formatTime(checkInTime));
            binding.includeTodayCard.tvCheckOutTime.setText(DateTimeFormatUtils.formatTime(checkOutTime));
            binding.includeTodayCard.tvWorkedHours.setText(DateTimeFormatUtils.formatWorkedHours(checkInTime, checkOutTime));

            CheckState checkState = CheckState.from(checkInTime, checkOutTime);
            binding.includeTodayCard.btnCheckInCta.setText(checkState.getButtonText());
            binding.includeTodayCard.btnCheckInCta.setEnabled(checkState.isButtonEnabled());
            binding.includeTodayCard.btnCheckInCta.setIconResource(R.drawable.ic_check_circle);
        } else {
            // Khi không có ca làm việc hôm nay: Vẫn hiển thị card với trạng thái ngày nghỉ
            binding.includeTodayCard.tvCheckInTime.setText("--:--");
            binding.includeTodayCard.tvCheckOutTime.setText("--:--");
            binding.includeTodayCard.tvWorkedHours.setText("0giờ 00phút");

            binding.includeTodayCard.btnCheckInCta.setText("Hôm nay nghỉ");
            binding.includeTodayCard.btnCheckInCta.setEnabled(false);
            binding.includeTodayCard.btnCheckInCta.setIconResource(R.drawable.ic_remove);
        }
    }

    private void renderTodayShiftCard(Shift todayShift) {
        if (binding == null) return;

        // Luôn hiển thị layout_home_today_shift
        binding.includeShiftToday.getRoot().setVisibility(View.VISIBLE);

        if (todayShift != null && todayShift.getDate().equals(LocalDate.now())) {
            // Có ca làm việc hôm nay
            binding.includeShiftToday.tvExchangeShift.setVisibility(View.VISIBLE);
            binding.includeShiftToday.tvShiftName.setText(todayShift.getShiftName());
            binding.includeShiftToday.tvShiftTime.setText(todayShift.getShiftTime());
            binding.includeShiftToday.tvShiftLocation.setText(todayShift.getLocation());

            if (binding.includeShiftToday.ivShiftIcon != null) {
                binding.includeShiftToday.ivShiftIcon.setImageResource(R.drawable.ic_schedule);
            }

            bindShiftStatusChip(todayShift.getStatus());
            if (getContext() != null) {
                ShiftAlarmScheduler.scheduleShiftAlarms(requireContext(), todayShift, sharedPreferenceApp);
            }
            binding.includeShiftToday.tvExchangeShift.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("selected_shift_id", todayShift.getId());
                args.putString("selected_shift_name", todayShift.getShiftName());
                if (todayShift.getDate() != null) {
                    args.putString("selected_shift_date", todayShift.getDate().toString());
                }
                navigateTo(R.id.action_employeeHomeFragment_to_employeeShiftExchangeFragment, args);
            });
        } else {
            // KHÔNG CÓ CA LÀM VIỆC HÔM NAY: Hiển thị 1 card thông điệp ngày nghỉ
            binding.includeShiftToday.tvExchangeShift.setVisibility(View.GONE);
            binding.includeShiftToday.tvShiftName.setText("Không có ca làm việc hôm nay, hãy tận hưởng ngày nghỉ");
            binding.includeShiftToday.tvShiftTime.setText("Nghỉ ngơi");
            binding.includeShiftToday.tvShiftLocation.setText("Chúc bạn một ngày thư giãn và nạp lại năng lượng!");

            if (binding.includeShiftToday.ivShiftIcon != null) {
                binding.includeShiftToday.ivShiftIcon.setImageResource(R.drawable.ic_leave);
            }

            // Chip trạng thái ngày nghỉ
            binding.includeShiftToday.tagShiftStatus.setText("Ngày nghỉ");
            int bgColor = ColorUtil.getThemeColor(requireContext(), R.attr.colorAttendanceLeaveContainer);
            int textColor = ColorUtil.getThemeColor(requireContext(), R.attr.colorAttendanceLeave);
            binding.includeShiftToday.tagShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
            binding.includeShiftToday.tagShiftStatus.setTextColor(textColor);
        }
    }

    private void bindShiftStatusChip(ShiftStatus status) {
        if (binding == null) return;
        ShiftStatus currentStatus = (status != null) ? status : ShiftStatus.PENDING;
        binding.includeShiftToday.tagShiftStatus.setText(currentStatus.getDisplayName());

        int bgColor = ColorUtil.getThemeColor(requireContext(), currentStatus.getBgAttrRes());
        int textColor = ColorUtil.getThemeColor(requireContext(), currentStatus.getTextColorAttrRes());

        binding.includeShiftToday.tagShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        binding.includeShiftToday.tagShiftStatus.setTextColor(textColor);
    }

    private void updateClock() {
        if (binding == null) return;
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
        employeeCategoryTypeAdapter = new EmployeeCategoryTypeAdapter(this::onCategoryClicked);

        RecyclerView rv = binding.includeCategories.rvCategories;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
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
        binding.includeWeek.rvWeekDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));
//        binding.includeWeek.rvWeekDays.setHasFixedSize(true);
        weekDayAdapter = new EmployeeWeekDayAdapter();
        weekDayAdapter.setOnDayClickListener(day -> {
            if (day != null && day.getStatus() != null && getContext() != null) {
                String dayTitle = day.isToday() ? "Hôm nay" : day.getDate().toString();
                Toast.makeText(getContext(), dayTitle + ": " + day.getStatus().getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        });
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
        navigateTo(actionId, null);
    }

    private void navigateTo(int actionId, Bundle args) {
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.employeeHomeFragment) {
            if (args != null) {
                navController.navigate(actionId, args);
            } else {
                navController.navigate(actionId);
            }
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
