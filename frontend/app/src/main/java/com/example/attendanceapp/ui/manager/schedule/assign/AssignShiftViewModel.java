package com.example.attendanceapp.ui.manager.schedule.assign;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData State
    private final MutableLiveData<List<ManagerEmployeeItem>> allEmployeesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ManagerEmployeeItem>> selectedEmployeesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ShiftOption>> shiftsLiveData = new MutableLiveData<>();
    private final MutableLiveData<ShiftOption> selectedShiftLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<LocalDate>> selectedDatesLiveData = new MutableLiveData<>(new ArrayList<>());

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
            ExportScheduleUseCase exportScheduleUseCase
    ) {
        this.getShiftsUseCase = getShiftsUseCase;
        this.getEmployeeWorkSchedulesUseCase = getEmployeeWorkSchedulesUseCase;
        this.addShiftUseCase = addShiftUseCase;
        this.assignShiftUseCase = assignShiftUseCase;
        this.observeShiftsUseCase = observeShiftsUseCase;
        this.observeSchedulesUseCase = observeSchedulesUseCase;
        this.exportScheduleUseCase = exportScheduleUseCase;

        observeRepositoryStreams();
        loadShifts();
        loadEmployees();
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
                                    Map<String, ManagerEmployeeItem> distinct = new LinkedHashMap<>();
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
        return new ManagerEmployeeItem(
                id,
                "NV-" + id.substring(Math.max(0, id.length() - 4)),
                schedule.getName(),
                schedule.getAvatar(),
                schedule.getPosition(),
                "Cửa hàng Q1",
                "",
                "",
                EmployeeStatus.fromString(schedule.getStatus()),
                0, 0, 0
        );
    }

    public void toggleEmployeeSelection(ManagerEmployeeItem employee) {
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
        isLoadingLiveData.setValue(true);
        disposables.add(
                addShiftUseCase.execute(shiftName, startTime, endTime, latitude, longitude)
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