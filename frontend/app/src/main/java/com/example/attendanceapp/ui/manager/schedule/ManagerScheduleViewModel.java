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
import com.example.attendanceapp.domain.usecase.schedule.ObserveEmployeeWorkSchedulesUseCase;
import com.example.attendanceapp.domain.usecase.shift.AssignShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.DeleteShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.GetShiftsUseCase;
import com.example.attendanceapp.domain.usecase.shift.ObserveShiftsUseCase;
import com.example.attendanceapp.domain.usecase.shift.UpdateShiftUseCase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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

    public static final String FILTER_UNASSIGNED = "UNASSIGNED";

    private final GetShiftsUseCase getShiftsUseCase;
    private final GetEmployeeWorkSchedulesUseCase getEmployeeWorkSchedulesUseCase;
    private final AssignShiftUseCase assignShiftUseCase;
    private final UpdateShiftUseCase updateShiftUseCase;
    private final DeleteShiftUseCase deleteShiftUseCase;
    private final ObserveShiftsUseCase observeShiftsUseCase;
    private final ObserveEmployeeWorkSchedulesUseCase observeSchedulesUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // State Holders
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<LocalDate> currentWeekMonday = new MutableLiveData<>(LocalDate.now().with(DayOfWeek.MONDAY));
    // null = Tất cả, FILTER_UNASSIGNED = Chưa xếp, còn lại = shiftId
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
                                    AssignShiftUseCase assignShiftUseCase,
                                    UpdateShiftUseCase updateShiftUseCase,
                                    DeleteShiftUseCase deleteShiftUseCase,
                                    ObserveShiftsUseCase observeShiftsUseCase,
                                    ObserveEmployeeWorkSchedulesUseCase observeSchedulesUseCase) {
        this.getShiftsUseCase = getShiftsUseCase;
        this.getEmployeeWorkSchedulesUseCase = getEmployeeWorkSchedulesUseCase;
        this.assignShiftUseCase = assignShiftUseCase;
        this.updateShiftUseCase = updateShiftUseCase;
        this.deleteShiftUseCase = deleteShiftUseCase;
        this.observeShiftsUseCase = observeShiftsUseCase;
        this.observeSchedulesUseCase = observeSchedulesUseCase;

        setupMediator();
        observeRepositoryStreams();
        refreshData();
    }

    public void refreshData() {
        loadShiftOptions();
        loadEmployeeWorkSchedules();
    }

    private void observeRepositoryStreams() {
        disposables.add(
                observeShiftsUseCase.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                options -> {
                                    shiftOptions.setValue(options);

                                    // Nếu ca đang dùng để lọc đã bị xóa thì quay về "Tất cả"
                                    String filter = selectedShiftFilter.getValue();
                                    if (filter != null && !FILTER_UNASSIGNED.equals(filter)) {
                                        boolean exists = false;
                                        for (ShiftOption o : options) {
                                            if (filter.equals(o.getId())) {
                                                exists = true;
                                                break;
                                            }
                                        }
                                        if (!exists) selectedShiftFilter.setValue(null);
                                    }
                                },
                                throwable -> errorMsg.setValue(throwable.getMessage())
                        )
        );

        disposables.add(
                observeSchedulesUseCase.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                rawSchedules::setValue,
                                throwable -> errorMsg.setValue(throwable.getMessage())
                        )
        );
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
        LocalDate date = selectedDate.getValue();
        String dateStr = (date != null) ? date.toString() : LocalDate.now().toString();

        Map<String, ShiftOption> shiftById = new HashMap<>();
        List<ShiftOption> currentShifts = shiftOptions.getValue();
        if (currentShifts != null) {
            for (ShiftOption option : currentShifts) {
                if (option.getId() != null) shiftById.put(option.getId(), option);
            }
        }

        List<ManagerEmployeeItem> filteredList = new ArrayList<>();
        if (schedules != null) {
            for (EmployeeWorkSchedule schedule : schedules) {
                if (schedule.getWorkDate() == null || !schedule.getWorkDate().equals(dateStr)) {
                    continue;
                }

                String shiftId = schedule.getShift() != null ? schedule.getShift().getShiftId() : null;

                // Xử lý kiểm tra xem ca còn tồn tại trong hệ thống hay không
                boolean isShiftExist = shiftId != null && shiftById.containsKey(shiftId);

                boolean matches;
                if (filter == null) {
                    matches = true;
                } else if (FILTER_UNASSIGNED.equals(filter)) {
                    // Hiển thị nếu nhân viên chưa được xếp ca HOẶC ca của họ đã bị xóa khỏi hệ thống
                    matches = (schedule.getShift() == null || !isShiftExist);
                } else {
                    matches = filter.equals(shiftId) && isShiftExist;
                }

                if (matches) {
                    filteredList.add(mapToManagerEmployeeItem(schedule, shiftById));
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

        if (Boolean.TRUE.equals(isAssignSuccess.getValue())) {
            isAssignSuccess.setValue(false);
        }
    }

    private ManagerEmployeeItem mapToManagerEmployeeItem(EmployeeWorkSchedule schedule,
                                                         Map<String, ShiftOption> shiftById) {
        String shiftName = null;
        if (schedule.getShift() != null) {
            String shiftId = schedule.getShift().getShiftId();
            // Nếu ca tồn tại trong danh sách ca hiện tại -> lấy tên ca mới nhất
            // Nếu ca đã bị xóa khỏi danh sách ca -> shiftName = null (Hiển thị "Chưa xếp ca")
            if (shiftId != null && shiftById.containsKey(shiftId)) {
                shiftName = shiftById.get(shiftId).getTitle();
            }
        }

        EmployeeStatus employeeStatus = EmployeeStatus.fromString(schedule.getStatus());

        ManagerEmployeeItem item = new ManagerEmployeeItem(
                schedule.getEmployeeId(),
                schedule.getEmployeeId(),
                schedule.getName(),
                schedule.getAvatar(),
                schedule.getPosition(),
                "", "", "",
                employeeStatus,
                0, 0, 0
        );
        item.setAssignedShiftName(shiftName); // Nếu shiftName == null -> Adapter sẽ render trạng thái Chưa xếp ca
        return item;
    }

    private Map<LocalDate, DayStatus> calculateDayStatuses(List<EmployeeWorkSchedule> schedules) {
        Map<LocalDate, DayStatus> statuses = new HashMap<>();
        LocalDate selected = selectedDate.getValue();
        if (selected == null) selected = LocalDate.now();

        LocalDate firstDayOfMonth = selected.withDayOfMonth(1);
        int dayOfWeekOfFirst = firstDayOfMonth.getDayOfWeek().getValue();
        LocalDate gridStartDate = firstDayOfMonth.minusDays(dayOfWeekOfFirst - 1);

        for (int i = 0; i < 42; i++) {
            LocalDate date = gridStartDate.plusDays(i);

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

    public void loadShiftOptions() {
        disposables.add(
                getShiftsUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                options -> { },
                                throwable -> errorMsg.setValue("Khởi tạo danh sách ca thất bại: " + throwable.getMessage())
                        )
        );
    }

    public void loadEmployeeWorkSchedules() {
        isLoading.setValue(true);
        errorMsg.setValue(null);

        disposables.add(
                getEmployeeWorkSchedulesUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                schedules -> isLoading.setValue(false),
                                error -> {
                                    isLoading.setValue(false);
                                    errorMsg.setValue(error.getMessage());
                                }
                        )
        );
    }

    public void updateShift(String shiftId, String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        isLoading.setValue(true);
        disposables.add(
                updateShiftUseCase.execute(shiftId, shiftName, startTime, endTime, latitude, longitude)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                updated -> isLoading.setValue(false),
                                throwable -> {
                                    isLoading.setValue(false);
                                    errorMsg.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    public void deleteShift(ShiftOption shift) {
        if (shift == null || shift.getId() == null) return;
        isLoading.setValue(true);
        disposables.add(
                deleteShiftUseCase.execute(shift.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> isLoading.setValue(false),
                                throwable -> {
                                    isLoading.setValue(false);
                                    errorMsg.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    public void assignWorkSchedule(String employeeId, String shiftId, String workDate) {
        isLoading.setValue(true);
        disposables.add(
                assignShiftUseCase.execute(Collections.singletonList(employeeId), shiftId, workDate, null)
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