package com.example.attendanceapp.ui.manager.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.domain.model.DayStatus;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ManagerRepository;
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
import java.util.LinkedHashMap;
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
    private final ManagerRepository managerRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // State Holders
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<LocalDate> currentWeekMonday = new MutableLiveData<>(LocalDate.now().with(DayOfWeek.MONDAY));
    // null = Tất cả, FILTER_UNASSIGNED = Chưa xếp, còn lại = shiftId
    private final MutableLiveData<String> selectedShiftFilter = new MutableLiveData<>(null);

    private final MutableLiveData<List<EmployeeWorkSchedule>> rawSchedules = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<StaffItemResponse>> companyStaffs = new MutableLiveData<>(new ArrayList<>());
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
                                    ObserveEmployeeWorkSchedulesUseCase observeSchedulesUseCase,
                                    ManagerRepository managerRepository) {
        this.getShiftsUseCase = getShiftsUseCase;
        this.getEmployeeWorkSchedulesUseCase = getEmployeeWorkSchedulesUseCase;
        this.assignShiftUseCase = assignShiftUseCase;
        this.updateShiftUseCase = updateShiftUseCase;
        this.deleteShiftUseCase = deleteShiftUseCase;
        this.observeShiftsUseCase = observeShiftsUseCase;
        this.observeSchedulesUseCase = observeSchedulesUseCase;
        this.managerRepository = managerRepository;

        setupMediator();
        observeRepositoryStreams();
        refreshData();
    }

    public void refreshData() {
        loadShiftOptions();
        loadEmployeeWorkSchedules();
        loadCompanyStaffs();
    }

    public void loadCompanyStaffs() {
        disposables.add(
                managerRepository.getStaffs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    if (resp != null && resp.getData() != null) {
                                        companyStaffs.setValue(resp.getData());
                                    }
                                },
                                throwable -> {}
                        )
        );
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
        uiState.addSource(companyStaffs, staffs -> updateUiState());
        uiState.addSource(selectedShiftFilter, filter -> updateUiState());
        uiState.addSource(selectedDate, date -> updateUiState());
        uiState.addSource(shiftOptions, options -> updateUiState());
        uiState.addSource(isLoading, loading -> updateUiState());
        uiState.addSource(errorMsg, error -> updateUiState());
        uiState.addSource(isAssignSuccess, success -> updateUiState());
    }

    private void updateUiState() {
        List<EmployeeWorkSchedule> schedules = rawSchedules.getValue();
        List<StaffItemResponse> staffs = companyStaffs.getValue();
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

        // 1. Thu thập tất cả nhân viên của công ty (kết hợp từ companyStaffs và rawSchedules)
        Map<String, ManagerEmployeeItem> employeeMap = new LinkedHashMap<>();

        if (staffs != null) {
            for (StaffItemResponse staff : staffs) {
                if (staff.getStaffId() != null && !staff.getStaffId().trim().isEmpty()) {
                    String empId = staff.getStaffId().trim();
                    String code = empId.length() >= 6
                            ? "NV" + empId.substring(0, 4).toUpperCase()
                            : "NV" + empId;
                    EmployeeStatus status = "deactive".equalsIgnoreCase(staff.getStatus())
                            ? EmployeeStatus.TERMINATED : EmployeeStatus.ACTIVE;
                    String pos = (staff.getPosition() != null && !staff.getPosition().trim().isEmpty())
                            ? staff.getPosition().trim() : "Nhân viên";
                    String avatar = (staff.getAvatar() != null && !staff.getAvatar().trim().isEmpty()
                            && !"null".equalsIgnoreCase(staff.getAvatar()) && !"none".equalsIgnoreCase(staff.getAvatar()))
                            ? staff.getAvatar().trim() : "";

                    employeeMap.put(empId, new ManagerEmployeeItem(
                            empId,
                            code,
                            staff.getNameStaff() != null ? staff.getNameStaff() : "Nhân viên",
                            avatar,
                            pos,
                            "", "", "",
                            status,
                            0, 0, 0
                    ));
                }
            }
        }

        if (schedules != null) {
            for (EmployeeWorkSchedule schedule : schedules) {
                if (schedule.getEmployeeId() != null && !schedule.getEmployeeId().trim().isEmpty()) {
                    String empId = schedule.getEmployeeId().trim();
                    if (!employeeMap.containsKey(empId)) {
                        String code = empId.length() >= 6
                                ? "NV" + empId.substring(0, 4).toUpperCase()
                                : "NV" + empId;
                        EmployeeStatus status = EmployeeStatus.fromString(schedule.getStatus());
                        String pos = (schedule.getPosition() != null && !schedule.getPosition().trim().isEmpty())
                                ? schedule.getPosition().trim() : "Nhân viên";
                        String avatar = (schedule.getAvatar() != null && !schedule.getAvatar().trim().isEmpty()
                                && !"null".equalsIgnoreCase(schedule.getAvatar()) && !"none".equalsIgnoreCase(schedule.getAvatar()))
                                ? schedule.getAvatar().trim() : "";

                        employeeMap.put(empId, new ManagerEmployeeItem(
                                empId,
                                code,
                                schedule.getName() != null ? schedule.getName() : "Nhân viên",
                                avatar,
                                pos,
                                "", "", "",
                                status,
                                0, 0, 0
                        ));
                    }
                }
            }
        }

        // 2. Tìm ca làm việc của từng nhân viên trên ngày dateStr
        Map<String, EmployeeWorkSchedule> scheduleOnSelectedDate = new HashMap<>();
        if (schedules != null) {
            for (EmployeeWorkSchedule schedule : schedules) {
                if (schedule.getEmployeeId() != null && schedule.getWorkDate() != null
                        && schedule.getWorkDate().equals(dateStr) && schedule.getShift() != null) {
                    scheduleOnSelectedDate.put(schedule.getEmployeeId().trim(), schedule);
                }
            }
        }

        // 3. Xây dựng danh sách hiển thị và áp dụng bộ lọc (filter)
        List<ManagerEmployeeItem> filteredList = new ArrayList<>();
        for (Map.Entry<String, ManagerEmployeeItem> entry : employeeMap.entrySet()) {
            String empId = entry.getKey();
            ManagerEmployeeItem baseItem = entry.getValue();

            ManagerEmployeeItem item = new ManagerEmployeeItem(
                    baseItem.getUserId(),
                    baseItem.getEmployeeCode(),
                    baseItem.getFullName(),
                    baseItem.getAvatarUrl(),
                    baseItem.getPosition(),
                    baseItem.getDepartment(),
                    baseItem.getPhoneNumber(),
                    baseItem.getJoinDateLabel(),
                    baseItem.getStatus(),
                    baseItem.getShiftCountThisMonth(),
                    baseItem.getLeaveCountThisMonth(),
                    baseItem.getEstimatedSalaryThisMonth()
            );

            EmployeeWorkSchedule schedule = scheduleOnSelectedDate.get(empId);
            String shiftId = null;
            String shiftName = null;
            boolean isShiftExist = false;

            if (schedule != null && schedule.getShift() != null) {
                shiftId = schedule.getShift().getShiftId();
                if (shiftId != null && shiftById.containsKey(shiftId)) {
                    shiftName = shiftById.get(shiftId).getTitle();
                    isShiftExist = true;
                } else if (schedule.getShift().getShiftName() != null) {
                    shiftName = schedule.getShift().getShiftName();
                    isShiftExist = true;
                }
            }

            item.setAssignedShiftName(isShiftExist ? shiftName : null);

            boolean matches;
            if (filter == null) {
                matches = true;
            } else if (FILTER_UNASSIGNED.equals(filter)) {
                matches = (item.getAssignedShiftName() == null);
            } else {
                matches = (filter.equals(shiftId) && isShiftExist);
            }

            if (matches) {
                filteredList.add(item);
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

    private Map<LocalDate, DayStatus> calculateDayStatuses(List<EmployeeWorkSchedule> schedules) {
        Map<LocalDate, DayStatus> statuses = new HashMap<>();
        LocalDate selected = selectedDate.getValue();
        if (selected == null) selected = LocalDate.now();

        // Tính toán phạm vi hiển thị (thường là 42 ngày cho grid tháng)
        LocalDate firstDayOfMonth = selected.withDayOfMonth(1);
        int dayOfWeekOfFirst = firstDayOfMonth.getDayOfWeek().getValue();
        LocalDate gridStartDate = firstDayOfMonth.minusDays(dayOfWeekOfFirst - 1);

        for (int i = 0; i < 42; i++) {
            LocalDate date = gridStartDate.plusDays(i);

            long count = 0;
            if (schedules != null) {
                count = schedules.stream()
                        .filter(s -> s.getWorkDate() != null && date.toString().equals(s.getWorkDate()) && s.getShift() != null)
                        .count();
            }

            // Logic: Có ca -> Tích xanh, Không ca -> ! đỏ
            if (count > 0) {
                statuses.put(date, DayStatus.SUCCESS);
            } else {
                statuses.put(date, DayStatus.CRITICAL);
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
                                options -> {},
                                throwable -> errorMsg.setValue(throwable.getMessage())
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
        updateShift(shiftId, shiftName, startTime, endTime, startTime, 100.0, latitude, longitude);
    }

    public void updateShift(String shiftId, String shiftName, String startTime, String endTime,
                            String allowedCheckInTime, double checkInRadius,
                            Double latitude, Double longitude) {
        isLoading.setValue(true);
        disposables.add(
                updateShiftUseCase.execute(shiftId, shiftName, startTime, endTime, allowedCheckInTime, checkInRadius, latitude != null ? latitude : 0.0, longitude != null ? longitude : 0.0)
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
        List<EmployeeWorkSchedule> currentSchedules = rawSchedules.getValue();
        if (currentSchedules != null && workDate != null && employeeId != null) {
            for (EmployeeWorkSchedule item : currentSchedules) {
                if (employeeId.equals(item.getEmployeeId()) && workDate.equals(item.getWorkDate()) && item.getShift() != null) {
                    String shiftName = item.getShift().getShiftName();
                    errorMsg.setValue("Nhân viên này đã có ca làm (" + (shiftName != null ? shiftName : "Đã có ca") + ") trong ngày này. Mỗi nhân viên chỉ làm 1 ca/ngày!");
                    return;
                }
            }
        }
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
                                    loadCompanyStaffs();
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
