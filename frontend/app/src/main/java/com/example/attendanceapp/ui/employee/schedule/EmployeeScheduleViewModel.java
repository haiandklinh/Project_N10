package com.example.attendanceapp.ui.employee.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.data.mapper.StaffScheduleMapper;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.domain.usecase.schedule.GetScheduleByDateUseCase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Tải toàn bộ lịch của tôi một lần (API không có tham số ngày), giữ trong bộ nhớ.
 * Chuyển tuần hay chọn ngày chỉ tính lại từ danh sách đã có, không gọi mạng.
 */
@HiltViewModel
public class EmployeeScheduleViewModel extends ViewModel {

    private static final int UPCOMING_LIMIT = 10;

    private final GetScheduleByDateUseCase getScheduleByDateUseCase;
    private final SharedPreferenceApp sharedPreferenceApp;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<ScheduleUiState> scheduleUiState = new MutableLiveData<>();

    private List<Shift> allShifts = new ArrayList<>();
    private boolean loaded = false;
    private LocalDate selectedDate = LocalDate.now();
    private LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    @Inject
    public EmployeeScheduleViewModel(
            GetScheduleByDateUseCase getScheduleByDateUseCase,
            SharedPreferenceApp sharedPreferenceApp
    ) {
        this.getScheduleByDateUseCase = getScheduleByDateUseCase;
        this.sharedPreferenceApp = sharedPreferenceApp;
        load();
    }

    public LiveData<ScheduleUiState> getScheduleUiState() {
        return scheduleUiState;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    // ===== Intents từ UI =====

    /** Tải lại từ server (thử lại khi lỗi). */
    public void refresh() {
        load();
    }

    /** Tải lại ngầm không hiển thị trạng thái loading giật màn hình */
    public void refreshSilently() {
        disposables.add(
                getScheduleByDateUseCase.execute(null)
                        .map(schedules -> StaffScheduleMapper.toShifts(schedules, sharedPreferenceApp))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shifts -> {
                                    allShifts = shifts;
                                    loaded = true;
                                    publish();
                                },
                                error -> {}
                        )
        );
    }

    public void showPreviousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        publish();
    }

    public void showNextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        publish();
    }

    public void selectDate(LocalDate date) {
        selectedDate = date;
    }

    // ===== Xử lý =====

    private void load() {
        scheduleUiState.setValue(ScheduleUiState.loading());
        disposables.add(
                getScheduleByDateUseCase.execute(null) // null: lấy toàn bộ lịch
                        .map(schedules -> StaffScheduleMapper.toShifts(schedules, sharedPreferenceApp))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shifts -> {
                                    allShifts = shifts;
                                    loaded = true;
                                    publish();
                                },
                                error -> scheduleUiState.setValue(ScheduleUiState.error(error.getMessage()))
                        )
        );
    }

    private void publish() {
        if (!loaded) return;

        LocalDate today = LocalDate.now();
        List<Shift> weekShifts = StaffScheduleMapper.inRange(allShifts, currentWeekStart, currentWeekStart.plusDays(6));
        List<Shift> todayShifts = StaffScheduleMapper.pickTodayShifts(allShifts, today);
        Shift todayShift = StaffScheduleMapper.pickTodayShift(allShifts, today);

        scheduleUiState.setValue(ScheduleUiState.success(
                todayShift,
                todayShifts,
                StaffScheduleMapper.upcoming(allShifts, today, UPCOMING_LIMIT),
                currentWeekStart,
                StaffScheduleMapper.buildWeekDays(allShifts, currentWeekStart, today, sharedPreferenceApp),
                weekShifts.size(),
                StaffScheduleMapper.totalHours(weekShifts)
        ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    // ===== UI state =====

    public static class ScheduleUiState {
        private final boolean isLoading;
        private final String error;
        private final Shift todayShift;              // null nếu hôm nay không có ca
        private final List<Shift> todayShifts;
        private final List<Shift> upcomingShifts;
        private final LocalDate weekStart;
        private final List<WeekDay> weekDays;
        private final int totalShifts;               // số ca trong tuần đang xem
        private final double totalHours;             // tổng giờ trong tuần đang xem

        private ScheduleUiState(boolean isLoading, String error, Shift todayShift, List<Shift> todayShifts,
                                List<Shift> upcomingShifts, LocalDate weekStart,
                                List<WeekDay> weekDays, int totalShifts, double totalHours) {
            this.isLoading = isLoading;
            this.error = error;
            this.todayShift = todayShift;
            this.todayShifts = todayShifts != null ? todayShifts : (todayShift != null ? Collections.singletonList(todayShift) : Collections.emptyList());
            this.upcomingShifts = upcomingShifts;
            this.weekStart = weekStart;
            this.weekDays = weekDays;
            this.totalShifts = totalShifts;
            this.totalHours = totalHours;
        }

        public static ScheduleUiState loading() {
            return new ScheduleUiState(true, null, null, Collections.emptyList(), new ArrayList<>(), null, new ArrayList<>(), 0, 0);
        }

        public static ScheduleUiState error(String error) {
            return new ScheduleUiState(false, error != null ? error : "Đã có lỗi xảy ra",
                    null, Collections.emptyList(), new ArrayList<>(), null, new ArrayList<>(), 0, 0);
        }

        public static ScheduleUiState success(Shift todayShift, List<Shift> todayShifts, List<Shift> upcomingShifts,
                                              LocalDate weekStart, List<WeekDay> weekDays,
                                              int totalShifts, double totalHours) {
            return new ScheduleUiState(false, null, todayShift, todayShifts, upcomingShifts,
                    weekStart, weekDays, totalShifts, totalHours);
        }

        public static ScheduleUiState success(Shift todayShift, List<Shift> upcomingShifts,
                                              LocalDate weekStart, List<WeekDay> weekDays,
                                              int totalShifts, double totalHours) {
            return success(todayShift,
                    todayShift != null ? Collections.singletonList(todayShift) : Collections.emptyList(),
                    upcomingShifts, weekStart, weekDays, totalShifts, totalHours);
        }

        public boolean isLoading() { return isLoading; }
        public String getError() { return error; }
        public Shift getTodayShift() { return todayShift; }
        public List<Shift> getTodayShifts() { return todayShifts != null ? todayShifts : Collections.emptyList(); }
        public List<Shift> getUpcomingShifts() { return upcomingShifts; }
        public LocalDate getWeekStart() { return weekStart; }
        public List<WeekDay> getWeekDays() { return weekDays; }
        public int getTotalShifts() { return totalShifts; }
        public double getTotalHours() { return totalHours; }
    }
}
