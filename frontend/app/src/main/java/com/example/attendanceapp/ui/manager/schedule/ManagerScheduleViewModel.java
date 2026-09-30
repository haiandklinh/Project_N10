package com.example.attendanceapp.ui.manager.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.DayStatus;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.usecase.schedule.GetEmployeeWorkSchedulesUseCase;
import com.example.attendanceapp.domain.usecase.shift.AssignShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.GetShiftsUseCase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerScheduleViewModel extends ViewModel {

    private final GetShiftsUseCase getShiftsUseCase;
    private final GetEmployeeWorkSchedulesUseCase getEmployeeWorkSchedulesUseCase;
    private final AssignShiftUseCase assignShiftUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // State Holders
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<LocalDate> currentWeekMonday = new MutableLiveData<>(LocalDate.now().with(DayOfWeek.MONDAY));
    private final MutableLiveData<String> selectedShiftFilter = new MutableLiveData<>(null);

    private final MutableLiveData<List<EmployeeWorkSchedule>> rawSchedules = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ShiftOption>> shiftOptions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isAssignSuccess = new MutableLiveData<>(false);

    // Single source of truth for UI
    private final MediatorLiveData<ManagerScheduleUiState> uiState = new MediatorLiveData<>();

    @Inject
    public ManagerScheduleViewModel(GetShiftsUseCase getShiftsUseCase,
                                    GetEmployeeWorkSchedulesUseCase getEmployeeWorkSchedulesUseCase,
                                    AssignShiftUseCase assignShiftUseCase) {
        this.getShiftsUseCase = getShiftsUseCase;
        this.getEmployeeWorkSchedulesUseCase = getEmployeeWorkSchedulesUseCase;
        this.assignShiftUseCase = assignShiftUseCase;

        setupMediator();
        loadShiftOptions();
    }

    private void setupMediator() {
        uiState.addSource(rawSchedules, schedules -> updateUiState());
        uiState.addSource(selectedShiftFilter, filter -> updateUiState());
        uiState.addSource(selectedDate, date -> updateUiState());
        uiState.addSource(shiftOptions, options -> updateUiState());
        uiState.addSource(isLoading, loading -> updateUiState());
        uiState.addSource(errorMsg, error -> updateUiState());
        uiState.addSource(isAssignSuccess, success -> updateUiState());
    }

    private void updateUiState() {
        List<EmployeeWorkSchedule> schedules = rawSchedules.getValue();
        String filter = selectedShiftFilter.getValue();

        List<ManagerEmployeeItem> filteredList = new ArrayList<>();
        if (schedules != null) {
            for (EmployeeWorkSchedule schedule : schedules) {
                ManagerEmployeeItem item = mapToManagerEmployeeItem(schedule);

                if (filter == null) {
                    filteredList.add(item);
                } else if ("UNASSIGNED".equals(filter)) {
                    if (item.getAssignedShiftName() == null) {
                        filteredList.add(item);
                    }
                } else if (filter.equals(item.getAssignedShiftName())) {
                    filteredList.add(item);
                }
            }
        }

        Map<LocalDate, DayStatus> statuses = calculateDayStatuses(schedules);

        uiState.setValue(new ManagerScheduleUiState(
                Boolean.TRUE.equals(isLoading.getValue()),
                errorMsg.getValue(),
                filteredList,
                shiftOptions.getValue(),
                statuses,
                Boolean.TRUE.equals(isAssignSuccess.getValue())
        ));
    }

    private ManagerEmployeeItem mapToManagerEmployeeItem(EmployeeWorkSchedule schedule) {
        String shiftName = null;
        if (schedule.getShift() != null) {
            shiftName = schedule.getShift().getShiftName();
        }

        EmployeeStatus employeeStatus = EmployeeStatus.fromString(schedule.getStatus());

        ManagerEmployeeItem item = new ManagerEmployeeItem(
                schedule.getEmployeeId(),
                schedule.getEmployeeId(), // Dùng employeeId làm employeeCode tạm thời
                schedule.getName(),
                schedule.getAvatar(),
                schedule.getPosition(),
                "", "", "",
                employeeStatus,
                0, 0, 0
        );
        item.setAssignedShiftName(shiftName);
        return item;
    }

    private Map<LocalDate, DayStatus> calculateDayStatuses(List<EmployeeWorkSchedule> schedules) {
        Map<LocalDate, DayStatus> statuses = new HashMap<>();
        LocalDate monday = currentWeekMonday.getValue();
        if (monday == null) return statuses;

        for (int i = 0; i < 42; i++) { // Tính toán cho cả view tuần và tháng
            LocalDate date = monday.minusDays(7).plusDays(i);

            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                statuses.put(date, DayStatus.CRITICAL);
                continue;
            }

            long count = 0;
            if (schedules != null) {
                count = schedules.stream()
                        .filter(s -> date.toString().equals(s.getWorkDate()))
                        .count();
            }

            if (count == 0) {
                statuses.put(date, DayStatus.CRITICAL);
            } else if (date.getDayOfMonth() == 15 || date.getDayOfMonth() == 20) {
                statuses.put(date, DayStatus.WARNING);
            } else {
                statuses.put(date, DayStatus.SUCCESS);
            }
        }
        return statuses;
    }

    /**
     * Nạp danh sách các ca làm việc (ShiftOption) qua RxJava3 Single
     */
    public void loadShiftOptions() {
        disposables.add(
                getShiftsUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                options -> shiftOptions.setValue(options),
                                throwable -> errorMsg.setValue("Khởi tạo danh sách ca thất bại: " + throwable.getMessage())
                        )
        );
    }

    public void loadEmployeeWorkSchedules() {
        isLoading.setValue(true);
        errorMsg.setValue(null);
        isAssignSuccess.setValue(false);

        disposables.add(
                getEmployeeWorkSchedulesUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                schedules -> {
                                    isLoading.setValue(false);
                                    rawSchedules.setValue(schedules);
                                },
                                error -> {
                                    isLoading.setValue(false);
                                    errorMsg.setValue(error.getMessage());
                                }
                        )
        );
    }

    public void assignWorkSchedule(String employeeId, String name, String position, String shiftId, String workDate) {
        isLoading.setValue(true);
        disposables.add(
                assignShiftUseCase.execute(employeeId, name, position, shiftId, workDate)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                schedule -> {
                                    isLoading.setValue(false);
                                    isAssignSuccess.setValue(true);
                                    loadEmployeeWorkSchedules();
                                },
                                error -> {
                                    isLoading.setValue(false);
                                    errorMsg.setValue(error.getMessage());
                                }
                        )
        );
    }

    // User Actions
    public void setSelectedDate(LocalDate date) {
        this.selectedDate.setValue(date);
        this.currentWeekMonday.setValue(date.with(DayOfWeek.MONDAY));
    }

    public void setFilter(String filter) {
        this.selectedShiftFilter.setValue(filter);
    }

    public void prevWeekOrMonth(boolean isWeekMode) {
        LocalDate current = selectedDate.getValue();
        if (current == null) return;

        if (isWeekMode) {
            LocalDate monday = currentWeekMonday.getValue();
            if (monday != null) {
                LocalDate newMonday = monday.minusWeeks(1);
                currentWeekMonday.setValue(newMonday);
                selectedDate.setValue(newMonday);
            }
        } else {
            LocalDate newDate = current.minusMonths(1).withDayOfMonth(1);
            selectedDate.setValue(newDate);
            currentWeekMonday.setValue(newDate.with(DayOfWeek.MONDAY));
        }
    }

    public void nextWeekOrMonth(boolean isWeekMode) {
        LocalDate current = selectedDate.getValue();
        if (current == null) return;

        if (isWeekMode) {
            LocalDate monday = currentWeekMonday.getValue();
            if (monday != null) {
                LocalDate newMonday = monday.plusWeeks(1);
                currentWeekMonday.setValue(newMonday);
                selectedDate.setValue(newMonday);
            }
        } else {
            LocalDate newDate = current.plusMonths(1).withDayOfMonth(1);
            selectedDate.setValue(newDate);
            currentWeekMonday.setValue(newDate.with(DayOfWeek.MONDAY));
        }
    }

    // Getters
    public LiveData<ManagerScheduleUiState> getUiState() { return uiState; }
    public LiveData<LocalDate> getSelectedDate() { return selectedDate; }
    public LiveData<LocalDate> getCurrentWeekMonday() { return currentWeekMonday; }
    public String getSelectedShiftFilter() { return selectedShiftFilter.getValue(); }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}