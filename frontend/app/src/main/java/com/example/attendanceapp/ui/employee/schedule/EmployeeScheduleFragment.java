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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.notification.ShiftAlarmScheduler;
import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.FragmentEmployeeScheduleBinding;
import com.example.attendanceapp.databinding.LayoutScheduleTimelineBinding;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.ui.employee.home.shift.EmployeeTodayShiftAdapter;
import com.example.attendanceapp.ui.employee.schedule.shift.EmployeeUpcomingShiftAdapter;
import com.example.attendanceapp.ui.employee.schedule.weekday.EmployeeScheduleWeekDayAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class EmployeeScheduleFragment extends Fragment {

    private FragmentEmployeeScheduleBinding binding;
    private EmployeeScheduleWeekDayAdapter employeeScheduleWeekDayAdapter;
    private EmployeeUpcomingShiftAdapter shiftAdapter;
    private EmployeeTodayShiftAdapter todayShiftAdapter;
    private EmployeeScheduleViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private io.reactivex.rxjava3.disposables.Disposable autoRefreshDisposable;

    @Inject
    WebSocketNotificationManager webSocketManager;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    public EmployeeScheduleFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EmployeeScheduleViewModel.class);

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

        setupSwipeRefresh();
        setupWeekDaysRecyclerView();
        setupTodayShiftActions();
        setupUpcomingShiftRecyclerView();
        setupWeekNavigator();
        setupQuickActions();
        setupObservers();
        listenToWebSocketNotifications();
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void listenToWebSocketNotifications() {
        if (webSocketManager != null) {
            disposables.add(webSocketManager.observeIncomingNotifications()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(notification -> {
                        // Tự động tải lại lịch khi nhận thông báo liên quan đến ca làm
                        if (viewModel != null) {
                            viewModel.refresh();
                        }
                    }, throwable -> {}));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tự động đồng bộ ca làm việc mới nhất khi nhân viên quay lại màn hình
        if (viewModel != null) {
            viewModel.refresh();
        }
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshDisposable = io.reactivex.rxjava3.core.Observable.interval(15, 15, java.util.concurrent.TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> {
                    if (viewModel != null) {
                        viewModel.refreshSilently();
                    }
                }, throwable -> {});
    }

    private void stopAutoRefresh() {
        if (autoRefreshDisposable != null && !autoRefreshDisposable.isDisposed()) {
            autoRefreshDisposable.dispose();
            autoRefreshDisposable = null;
        }
    }

    // =====================================================================
    // SETUP VIEW (chỉ gắn adapter / listener, dữ liệu do ViewModel đẩy xuống)
    // =====================================================================

    private void setupWeekDaysRecyclerView() {
        LayoutScheduleTimelineBinding timelineBinding = binding.includeCardTimeline;

        employeeScheduleWeekDayAdapter = new EmployeeScheduleWeekDayAdapter(new ArrayList<>());
        timelineBinding.rvWeekDays.setAdapter(employeeScheduleWeekDayAdapter);

        employeeScheduleWeekDayAdapter.setOnDayClickListener(day -> viewModel.selectDate(day.getDate()));
    }

    private void setupTodayShiftActions() {
        if (binding.includeShiftToday == null) return;

        if (binding.includeShiftToday.rvTodayShifts != null) {
            binding.includeShiftToday.rvTodayShifts.setLayoutManager(new LinearLayoutManager(requireContext()));
            todayShiftAdapter = new EmployeeTodayShiftAdapter(shift -> {
                if (shift == null) return;
                Bundle args = new Bundle();
                args.putString("selected_shift_id", shift.getId());
                args.putString("selected_shift_name", shift.getShiftName());
                if (shift.getDate() != null) {
                    args.putString("selected_shift_date", shift.getDate().toString());
                }
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment, args);
            });
            binding.includeShiftToday.rvTodayShifts.setAdapter(todayShiftAdapter);
        }

        if (binding.includeShiftToday.tvExchangeShift != null) {
            binding.includeShiftToday.tvExchangeShift.setOnClickListener(v ->
                    Navigation.findNavController(v)
                            .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment));
        }
    }

    private void setupUpcomingShiftRecyclerView() {
        shiftAdapter = new EmployeeUpcomingShiftAdapter();

        binding.rvUpcomingShifts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingShifts.setNestedScrollingEnabled(false);
        binding.rvUpcomingShifts.setAdapter(shiftAdapter);

        shiftAdapter.setOnItemClickListener((shift, position) -> {
            if (shift != null) {
                Bundle args = new Bundle();
                args.putString("selected_shift_id", shift.getId());
                args.putString("selected_shift_name", shift.getShiftName());
                if (shift.getDate() != null) {
                    args.putString("selected_shift_date", shift.getDate().toString());
                }
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment, args);
            }
        });
    }

    private void setupWeekNavigator() {
        LayoutScheduleTimelineBinding timelineBinding = binding.includeCardTimeline;

        timelineBinding.btnPrevWeek.setOnClickListener(v -> viewModel.showPreviousWeek());
        timelineBinding.btnNextWeek.setOnClickListener(v -> viewModel.showNextWeek());
    }

    private void setupQuickActions() {
        binding.btnSwapShift.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment));

        binding.btnRequestLeave.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_employeeScheduleFragment_to_employeeLeaveHistoryFragment));
    }

    // =====================================================================
    // OBSERVE & RENDER
    // =====================================================================

    private void setupObservers() {
        viewModel.getScheduleUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading()) {
                binding.swipeRefreshLayout.setRefreshing(true);
                return;
            }
            binding.swipeRefreshLayout.setRefreshing(false);
            if (state.getError() != null) {
                Toast.makeText(requireContext(), "Lỗi: " + state.getError(), Toast.LENGTH_SHORT).show();
                return;
            }
            render(state);
        });
    }

    private void render(EmployeeScheduleViewModel.ScheduleUiState state) {
        bindTodayShifts(state.getTodayShifts(), state.getTodayShift());
        shiftAdapter.submitList(state.getUpcomingShifts());

        employeeScheduleWeekDayAdapter.submitList(state.getWeekDays());
        updateWeekRangeTitle(state.getWeekStart());
        updateWeekSummary(state.getTotalShifts(), state.getTotalHours());
    }

    private void bindTodayShifts(List<Shift> todayShifts, @Nullable Shift fallbackShift) {
        if (binding.includeShiftToday == null) return;

        List<Shift> shiftsToDisplay = todayShifts;
        if ((shiftsToDisplay == null || shiftsToDisplay.isEmpty()) && fallbackShift != null) {
            shiftsToDisplay = Collections.singletonList(fallbackShift);
        }

        if (shiftsToDisplay == null || shiftsToDisplay.isEmpty()) {
            if (binding.includeShiftToday.includeCardNoShift.cardNoShift != null) {
                binding.includeShiftToday.includeCardNoShift.cardNoShift.setVisibility(View.VISIBLE);
            }
            if (binding.includeShiftToday.rvTodayShifts != null) {
                binding.includeShiftToday.rvTodayShifts.setVisibility(View.GONE);
            }
            if (binding.includeShiftToday.tvExchangeShift != null) {
                binding.includeShiftToday.tvExchangeShift.setVisibility(View.GONE);
            }
            return;
        }

        if (binding.includeShiftToday.includeCardNoShift.cardNoShift != null) {
            binding.includeShiftToday.includeCardNoShift.cardNoShift.setVisibility(View.GONE);
        }
        if (binding.includeShiftToday.rvTodayShifts != null) {
            binding.includeShiftToday.rvTodayShifts.setVisibility(View.VISIBLE);
        }
        if (binding.includeShiftToday.tvExchangeShift != null) {
            binding.includeShiftToday.tvExchangeShift.setVisibility(View.VISIBLE);
            Shift firstShift = shiftsToDisplay.get(0);
            binding.includeShiftToday.tvExchangeShift.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("selected_shift_id", firstShift.getId());
                args.putString("selected_shift_name", firstShift.getShiftName());
                if (firstShift.getDate() != null) {
                    args.putString("selected_shift_date", firstShift.getDate().toString());
                }
                Navigation.findNavController(v)
                        .navigate(R.id.action_employeeScheduleFragment_to_employeeShiftExchangeFragment, args);
            });
        }

        if (todayShiftAdapter != null) {
            todayShiftAdapter.submitList(shiftsToDisplay);
        }

        if (getContext() != null) {
            for (Shift s : shiftsToDisplay) {
                ShiftAlarmScheduler.scheduleShiftAlarms(requireContext(), s, sharedPreferenceApp);
            }
        }
    }

    private void bindTodayShift(@Nullable Shift todayShift) {
        bindTodayShifts(todayShift != null ? Collections.singletonList(todayShift) : Collections.emptyList(), todayShift);
    }

    private void updateWeekRangeTitle(@Nullable LocalDate weekStart) {
        if (weekStart == null) return;

        LocalDate weekEnd = weekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM", new Locale("vi", "VN"));

        String rangeText = weekStart.format(formatter) + " – "
                + weekEnd.format(formatter) + ", "
                + weekStart.getYear();

        binding.includeCardTimeline.tvWeekRange.setText(rangeText);
    }

    private void updateWeekSummary(int totalShifts, double totalHours) {
        LayoutScheduleTimelineBinding timelineBinding = binding.includeCardTimeline;
        timelineBinding.tvTotalShifts.setText(totalShifts + " ca");
        timelineBinding.tvTotalHours.setText(String.format(Locale.US, "%.1f giờ", totalHours));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
        disposables.clear();
        binding = null;
    }
}
