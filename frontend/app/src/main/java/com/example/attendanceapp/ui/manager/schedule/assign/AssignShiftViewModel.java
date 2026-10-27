package com.example.attendanceapp.ui.manager.schedule.assign;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.usecase.schedule.ExportScheduleUseCase;
import com.example.attendanceapp.domain.usecase.schedule.GetEmployeeWorkSchedulesUseCase;
import com.example.attendanceapp.domain.usecase.schedule.ObserveEmployeeWorkSchedulesUseCase;
import com.example.attendanceapp.domain.usecase.shift.AddShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.AssignShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.GetShiftsUseCase;
import com.example.attendanceapp.domain.usecase.shift.ObserveShiftsUseCase;

import android.net.Uri;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.domain.repository.ManagerRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class AssignShiftViewModel extends ViewModel {

    private final GetShiftsUseCase getShiftsUseCase;
    private final GetEmployeeWorkSchedulesUseCase getEmployeeWorkSchedulesUseCase;
    private final AddShiftUseCase addShiftUseCase;
    private final AssignShiftUseCase assignShiftUseCase;
    private final ObserveShiftsUseCase observeShiftsUseCase;
    private final ObserveEmployeeWorkSchedulesUseCase observeSchedulesUseCase;
    private final ExportScheduleUseCase exportScheduleUseCase;
    private final WebSocketNotificationManager webSocketManager;
    private final ManagerRepository managerRepository;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData State
    private final MutableLiveData<List<ManagerEmployeeItem>> allEmployeesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ManagerEmployeeItem>> selectedEmployeesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ShiftOption>> shiftsLiveData = new MutableLiveData<>();
    private final MutableLiveData<ShiftOption> selectedShiftLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<LocalDate>> selectedDatesLiveData = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<EmployeeWorkSchedule>> rawSchedulesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Map<String, String>> assignedShiftsMapLiveData = new MediatorLiveData<>();

    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> assignSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Uri> exportSuccessUriLiveData = new MutableLiveData<>();

    @Inject
    public AssignShiftViewModel(
            GetShiftsUseCase getShiftsUseCase,
            GetEmployeeWorkSchedulesUseCase getEmployeeWorkSchedulesUseCase,
            AddShiftUseCase addShiftUseCase,
            AssignShiftUseCase assignShiftUseCase,
            ObserveShiftsUseCase observeShiftsUseCase,
            ObserveEmployeeWorkSchedulesUseCase observeSchedulesUseCase,
            ExportScheduleUseCase exportScheduleUseCase,
            WebSocketNotificationManager webSocketManager,
            ManagerRepository managerRepository
    ) {
        this.getShiftsUseCase = getShiftsUseCase;
        this.getEmployeeWorkSchedulesUseCase = getEmployeeWorkSchedulesUseCase;
        this.addShiftUseCase = addShiftUseCase;
        this.assignShiftUseCase = assignShiftUseCase;
        this.observeShiftsUseCase = observeShiftsUseCase;
        this.observeSchedulesUseCase = observeSchedulesUseCase;
        this.exportScheduleUseCase = exportScheduleUseCase;
        this.webSocketManager = webSocketManager;
        this.managerRepository = managerRepository;

        assignedShiftsMapLiveData.addSource(rawSchedulesLiveData, schedules -> updateAssignedShiftsMap());
        assignedShiftsMapLiveData.addSource(selectedDatesLiveData, dates -> updateAssignedShiftsMap());

        observeRepositoryStreams();
        loadShifts();
        loadEmployees();
    }

    private void updateAssignedShiftsMap() {
        List<LocalDate> dates = selectedDatesLiveData.getValue();
        LocalDate date = (dates != null && !dates.isEmpty()) ? dates.get(0) : null;
        if (date == null) {
            assignedShiftsMapLiveData.setValue(new HashMap<>());
            return;
        }

        String dateStr = date.toString();
        List<EmployeeWorkSchedule> schedules = rawSchedulesLiveData.getValue();
        Map<String, String> map = new HashMap<>();
        if (schedules != null) {
            for (EmployeeWorkSchedule s : schedules) {
                if (s.getEmployeeId() != null && dateStr.equals(s.getWorkDate()) && s.getShift() != null) {
                    String shiftName = s.getShift().getShiftName();
                    if (shiftName == null || shiftName.isEmpty()) {
                        shiftName = "Đã có ca";
                    }
                    map.put(s.getEmployeeId(), shiftName);
                }
            }
        }
        assignedShiftsMapLiveData.setValue(map);

        // Tự động bỏ chọn các nhân viên đã có ca trong ngày đang chọn
        List<ManagerEmployeeItem> currentSelected = selectedEmployeesLiveData.getValue();
        if (currentSelected != null && !currentSelected.isEmpty()) {
            List<ManagerEmployeeItem> validSelected = new ArrayList<>();
            for (ManagerEmployeeItem emp : currentSelected) {
                if (!map.containsKey(emp.getUserId())) {
                    validSelected.add(emp);
                }
            }
            if (validSelected.size() != currentSelected.size()) {
                selectedEmployeesLiveData.setValue(validSelected);
            }
        }
    }

    public boolean hasShiftOnDate(String employeeId, String dateStr) {
        if (employeeId == null || dateStr == null) return false;
        List<EmployeeWorkSchedule> schedules = rawSchedulesLiveData.getValue();
        if (schedules == null) return false;
        for (EmployeeWorkSchedule s : schedules) {
            if (employeeId.equals(s.getEmployeeId()) && dateStr.equals(s.getWorkDate()) && s.getShift() != null) {
                return true;
            }
        }
        return false;
    }

    public void exportToExcel() {
        List<ManagerEmployeeItem> employees = selectedEmployeesLiveData.getValue();
        ShiftOption shift = selectedShiftLiveData.getValue();
        List<LocalDate> dates = selectedDatesLiveData.getValue();
        LocalDate date = (dates != null && !dates.isEmpty()) ? dates.get(0) : null;

        if (employees == null || employees.isEmpty() || shift == null || date == null) {
            errorMessageLiveData.setValue("Không có dữ liệu để xuất");
            return;
        }

        isLoadingLiveData.setValue(true);
        disposables.add(
                exportScheduleUseCase.execute(employees, shift, date)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                uri -> {
                                    isLoadingLiveData.setValue(false);
                                    exportSuccessUriLiveData.setValue(uri);
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue("Lỗi xuất file: " + throwable.getMessage());
                                }
                        )
        );
    }

    public void clearExportEvent() {
        exportSuccessUriLiveData.setValue(null);
    }

    private void observeRepositoryStreams() {
        disposables.add(
                observeShiftsUseCase.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shifts -> {
                                    shiftsLiveData.setValue(shifts);

                                    ShiftOption selected = selectedShiftLiveData.getValue();
                                    if (selected != null) {
                                        ShiftOption fresh = null;
                                        for (ShiftOption s : shifts) {
                                            if (s.getId() != null && s.getId().equals(selected.getId())) {
                                                fresh = s;
                                                break;
                                            }
                                        }
                                        selectedShiftLiveData.setValue(fresh);
                                    } else if (!shifts.isEmpty()) {
                                        selectedShiftLiveData.setValue(shifts.get(0));
                                    }
                                },
                                throwable -> errorMessageLiveData.setValue(throwable.getMessage())
                        )
        );

        disposables.add(
                observeSchedulesUseCase.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                schedules -> {
                                    rawSchedulesLiveData.setValue(schedules);
                                    Map<String, ManagerEmployeeItem> distinct = new LinkedHashMap<>();
                                    List<ManagerEmployeeItem> current = allEmployeesLiveData.getValue();
                                    if (current != null) {
                                        for (ManagerEmployeeItem item : current) {
                                            if (item.getUserId() != null) distinct.put(item.getUserId(), item);
                                        }
                                    }
                                    for (EmployeeWorkSchedule s : schedules) {
                                        if (s.getEmployeeId() != null && !distinct.containsKey(s.getEmployeeId())) {
                                            distinct.put(s.getEmployeeId(), mapToManagerEmployeeItem(s));
                                        }
                                    }
                                    allEmployeesLiveData.setValue(new ArrayList<>(distinct.values()));
                                },
                                throwable -> errorMessageLiveData.setValue("Lỗi tải nhân viên: " + throwable.getMessage())
                        )
        );
    }

    public void loadEmployees() {
        isLoadingLiveData.setValue(true);
        disposables.add(
                managerRepository.getStaffs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    if (resp != null && resp.getData() != null) {
                                        Map<String, ManagerEmployeeItem> map = new LinkedHashMap<>();
                                        for (StaffItemResponse s : resp.getData()) {
                                            if (s.getStaffId() != null) {
                                                String empId = s.getStaffId();
                                                String code = empId.length() >= 6 ? "NV" + empId.substring(0, 4).toUpperCase() : "NV001";
                                                String pos = s.getPosition() != null && !s.getPosition().isEmpty() ? s.getPosition() : "Nhân viên";
                                                String avatar = (s.getAvatar() != null && !s.getAvatar().isEmpty() && !"null".equalsIgnoreCase(s.getAvatar()) && !"none".equalsIgnoreCase(s.getAvatar())) ? s.getAvatar() : "";
                                                EmployeeStatus status = "deactive".equalsIgnoreCase(s.getStatus()) ? EmployeeStatus.TERMINATED : EmployeeStatus.ACTIVE;
                                                map.put(empId, new ManagerEmployeeItem(
                                                        empId, code, s.getNameStaff() != null ? s.getNameStaff() : "Nhân viên",
                                                        avatar, pos, "Cửa hàng Q1", "", "", status, 0, 0, 0
                                                ));
                                            }
                                        }
                                        List<ManagerEmployeeItem> current = allEmployeesLiveData.getValue();
                                        if (current != null) {
                                            for (ManagerEmployeeItem item : current) {
                                                if (item.getUserId() != null && !map.containsKey(item.getUserId())) {
                                                    map.put(item.getUserId(), item);
                                                }
                                            }
                                        }
                                        allEmployeesLiveData.setValue(new ArrayList<>(map.values()));
                                    }
                                },
                                throwable -> {}
                        )
        );
        disposables.add(
                getEmployeeWorkSchedulesUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                schedules -> isLoadingLiveData.setValue(false),
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue("Lỗi tải nhân viên: " + throwable.getMessage());
                                }
                        )
        );
    }

    private ManagerEmployeeItem mapToManagerEmployeeItem(EmployeeWorkSchedule schedule) {
        String id = schedule.getEmployeeId();
        String code = id != null && id.length() >= 6 ? "NV" + id.substring(0, 4).toUpperCase() : "NV001";
        String avatar = (schedule.getAvatar() != null && !schedule.getAvatar().isEmpty()
                && !"null".equalsIgnoreCase(schedule.getAvatar()) && !"none".equalsIgnoreCase(schedule.getAvatar()))
                ? schedule.getAvatar() : "";
        return new ManagerEmployeeItem(
                id,
                code,
                schedule.getName() != null ? schedule.getName() : "Nhân viên",
                avatar,
                schedule.getPosition() != null ? schedule.getPosition() : "Nhân viên",
                "Cửa hàng Q1",
                "",
                "",
                EmployeeStatus.fromString(schedule.getStatus()),
                0, 0, 0
        );
    }

    public void toggleEmployeeSelection(ManagerEmployeeItem employee) {
        if (employee == null || employee.getUserId() == null) return;

        Map<String, String> assignedMap = assignedShiftsMapLiveData.getValue();
        if (assignedMap != null && assignedMap.containsKey(employee.getUserId())) {
            errorMessageLiveData.setValue("Nhân viên " + employee.getFullName() + " đã có ca làm trong ngày này. Mỗi nhân viên chỉ làm 1 ca/ngày!");
            return;
        }

        List<ManagerEmployeeItem> currentSelected = new ArrayList<>(
                selectedEmployeesLiveData.getValue() != null ? selectedEmployeesLiveData.getValue() : new ArrayList<>());

        boolean exists = false;
        for (int i = 0; i < currentSelected.size(); i++) {
            if (currentSelected.get(i).getUserId().equals(employee.getUserId())) {
                currentSelected.remove(i);
                exists = true;
                break;
            }
        }

        if (!exists) {
            currentSelected.add(employee);
        }

        selectedEmployeesLiveData.setValue(currentSelected);
    }

    public void setSelectedEmployees(List<ManagerEmployeeItem> employees) {
        if (employees != null) {
            selectedEmployeesLiveData.setValue(new ArrayList<>(employees));
        }
    }

    public void removeEmployee(int position) {
        List<ManagerEmployeeItem> currentList = selectedEmployeesLiveData.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            List<ManagerEmployeeItem> newList = new ArrayList<>(currentList);
            newList.remove(position);
            selectedEmployeesLiveData.setValue(newList);
        }
    }

    public void startNewFlow(LocalDate date) {
        selectedEmployeesLiveData.setValue(new ArrayList<>());
        List<LocalDate> dates = new ArrayList<>();
        if (date != null) dates.add(date);
        selectedDatesLiveData.setValue(dates);
        assignSuccessLiveData.setValue(null);
        errorMessageLiveData.setValue(null);
        loadEmployees();
    }

    public void resetAssignState() {
        selectedEmployeesLiveData.setValue(new ArrayList<>());
        selectedDatesLiveData.setValue(new ArrayList<>());
        assignSuccessLiveData.setValue(null);
    }

    public void loadShifts() {
        isLoadingLiveData.setValue(true);
        disposables.add(
                getShiftsUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shifts -> isLoadingLiveData.setValue(false),
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    public void addShift(String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        addShift(shiftName, startTime, endTime, startTime, 100.0, latitude, longitude);
    }

    public void addShift(String shiftName, String startTime, String endTime,
                         String allowedCheckInTime, double checkInRadius,
                         Double latitude, Double longitude) {
        isLoadingLiveData.setValue(true);
        disposables.add(
                addShiftUseCase.execute(shiftName, startTime, endTime, allowedCheckInTime, checkInRadius, latitude, longitude)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                created -> {
                                    isLoadingLiveData.setValue(false);
                                    selectedShiftLiveData.setValue(created);
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    public void submitAssignShifts(String note) {
        List<ManagerEmployeeItem> employees = selectedEmployeesLiveData.getValue();
        ShiftOption selectedShift = selectedShiftLiveData.getValue();
        List<LocalDate> selectedDates = selectedDatesLiveData.getValue();

        if (employees == null || employees.isEmpty()) {
            errorMessageLiveData.setValue("Danh sách nhân viên không được để trống!");
            return;
        }

        if (selectedShift == null) {
            errorMessageLiveData.setValue("Vui lòng chọn 1 ca làm việc!");
            return;
        }

        if (selectedDates == null || selectedDates.isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng chọn ít nhất một ngày làm việc!");
            return;
        }

        // Ràng buộc nghiệp vụ: 1 nhân viên 1 ngày chỉ được làm tối đa 1 ca
        for (LocalDate date : selectedDates) {
            String dateStr = date.toString();
            for (ManagerEmployeeItem emp : employees) {
                if (hasShiftOnDate(emp.getUserId(), dateStr)) {
                    String dateFormatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    errorMessageLiveData.setValue("Nhân viên " + emp.getFullName() + " đã có ca làm vào ngày " + dateFormatted + ". Mỗi nhân viên chỉ được làm tối đa 1 ca/ngày!");
                    return;
                }
            }
        }

        List<String> employeeIds = new ArrayList<>();
        for (ManagerEmployeeItem emp : employees) {
            employeeIds.add(emp.getUserId());
        }

        isLoadingLiveData.setValue(true);

        // Sử dụng RxJava để xử lý xếp ca cho nhiều ngày
        disposables.add(
                Observable.fromIterable(selectedDates)
                        .concatMapSingle(date -> assignShiftUseCase.execute(
                                employeeIds,
                                selectedShift.getId(),
                                date.toString(),
                                note
                        ))
                        .toList() // Đợi tất cả hoàn thành
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                results -> {
                                    isLoadingLiveData.setValue(false);
                                    assignSuccessLiveData.setValue(true);
                                    if (webSocketManager != null) {
                                        String dateStr = (selectedDates != null && !selectedDates.isEmpty()) ? selectedDates.get(0).toString() : "";
                                        webSocketManager.notifyScheduleAssigned(dateStr);
                                    }
                                    loadEmployees();
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue("Lỗi khi xếp ca: " + throwable.getMessage());
                                }
                        )
        );
    }

    public void toggleDateSelection(LocalDate date) {
        List<LocalDate> current = new ArrayList<>(selectedDatesLiveData.getValue() != null 
                ? selectedDatesLiveData.getValue() : new ArrayList<>());
        if (current.contains(date)) {
            current.remove(date);
        } else {
            current.add(date);
        }
        selectedDatesLiveData.setValue(current);
    }

    public void selectFullWeek(LocalDate monday) {
        List<LocalDate> week = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            week.add(monday.plusDays(i));
        }
        selectedDatesLiveData.setValue(week);
    }

    public void selectWorkDays(LocalDate monday) {
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            weekDays.add(monday.plusDays(i));
        }
        selectedDatesLiveData.setValue(weekDays);
    }

    public void clearAssignSuccessEvent() {
        assignSuccessLiveData.setValue(null);
    }

    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }

    public void selectShift(ShiftOption shift) {
        selectedShiftLiveData.setValue(shift);
    }

    public void setSelectedDates(List<LocalDate> dates) {
        selectedDatesLiveData.setValue(dates);
    }

    public LiveData<List<ManagerEmployeeItem>> getAllEmployeesLiveData() { return allEmployeesLiveData; }
    public LiveData<List<ManagerEmployeeItem>> getSelectedEmployeesLiveData() { return selectedEmployeesLiveData; }
    public LiveData<Map<String, String>> getAssignedShiftsMapLiveData() { return assignedShiftsMapLiveData; }
    public LiveData<List<ShiftOption>> getShiftsLiveData() { return shiftsLiveData; }
    public LiveData<ShiftOption> getSelectedShiftLiveData() { return selectedShiftLiveData; }
    public LiveData<List<LocalDate>> getSelectedDatesLiveData() { return selectedDatesLiveData; }

    public LiveData<LocalDate> getSelectedDateLiveData() {
        return Transformations.map(selectedDatesLiveData, dates ->
                (dates != null && !dates.isEmpty()) ? dates.get(0) : null
        );
    }

    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<Boolean> getAssignSuccessLiveData() { return assignSuccessLiveData; }
    public LiveData<String> getErrorMessageLiveData() { return errorMessageLiveData; }
    public LiveData<Uri> getExportSuccessUriLiveData() { return exportSuccessUriLiveData; }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
