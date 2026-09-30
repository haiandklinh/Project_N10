package com.example.attendanceapp.ui.manager.schedule.assign;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.usecase.shift.AddShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.AssignShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.DeleteShiftUseCase;
import com.example.attendanceapp.domain.usecase.shift.GetShiftsUseCase;
import com.example.attendanceapp.domain.usecase.shift.UpdateShiftUseCase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class AssignShiftViewModel extends ViewModel {

    private final GetShiftsUseCase getShiftsUseCase;
    private final AddShiftUseCase addShiftUseCase;
    private final UpdateShiftUseCase updateShiftUseCase;
    private final DeleteShiftUseCase deleteShiftUseCase;
    private final AssignShiftUseCase assignShiftUseCase;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData State
    private final MutableLiveData<List<ManagerEmployeeItem>> selectedEmployeesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ShiftOption>> shiftsLiveData = new MutableLiveData<>();
    private final MutableLiveData<ShiftOption> selectedShiftLiveData = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> selectedDateLiveData = new MutableLiveData<>(LocalDate.now());

    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> assignSuccessLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    @Inject
    public AssignShiftViewModel(
            GetShiftsUseCase getShiftsUseCase,
            AddShiftUseCase addShiftUseCase,
            UpdateShiftUseCase updateShiftUseCase,
            DeleteShiftUseCase deleteShiftUseCase,
            AssignShiftUseCase assignShiftUseCase,
            SavedStateHandle savedStateHandle
    ) {
        this.getShiftsUseCase = getShiftsUseCase;
        this.addShiftUseCase = addShiftUseCase;
        this.updateShiftUseCase = updateShiftUseCase;
        this.deleteShiftUseCase = deleteShiftUseCase;
        this.assignShiftUseCase = assignShiftUseCase;

        loadShifts();
    }

    // --- QUẢN LÝ NHÂN VIÊN ---
    public void setSelectedEmployees(List<ManagerEmployeeItem> employees) {
        if (employees != null) {
            selectedEmployeesLiveData.setValue(new ArrayList<>(employees));
        }
    }

    public void removeEmployee(int position) {
        List<ManagerEmployeeItem> currentList = selectedEmployeesLiveData.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            selectedEmployeesLiveData.setValue(currentList);
        }
    }

    // --- GỌI USECASE KẾT NỐI API TRỰC TIẾP QUA RXJAVA3 ---

    /**
     * 1. Lấy danh sách ca làm
     */
    public void loadShifts() {
        isLoadingLiveData.setValue(true);
        disposables.add(
                getShiftsUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shifts -> {
                                    isLoadingLiveData.setValue(false);
                                    shiftsLiveData.setValue(shifts);
                                    if (!shifts.isEmpty() && selectedShiftLiveData.getValue() == null) {
                                        selectedShiftLiveData.setValue(shifts.get(0));
                                    }
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    /**
     * 2. Thêm ca làm mới
     */
    public void addShift(String shiftName, String startTime, String endTime, String description) {
        isLoadingLiveData.setValue(true);
        disposables.add(
                addShiftUseCase.execute(shiftName, startTime, endTime, description)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                newShift -> {
                                    isLoadingLiveData.setValue(false);
                                    selectedShiftLiveData.setValue(newShift);
                                    loadShifts(); // Tải lại danh sách ca làm mới
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    /**
     * 3. Cập nhật ca làm
     */
    public void updateShift(String shiftId, String shiftName, String startTime, String endTime, String description) {
        isLoadingLiveData.setValue(true);
        disposables.add(
                updateShiftUseCase.execute(shiftId, shiftName, startTime, endTime, description)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                updatedShift -> {
                                    isLoadingLiveData.setValue(false);
                                    selectedShiftLiveData.setValue(updatedShift);
                                    loadShifts();
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    /**
     * 4. Xóa ca làm
     */
    public void deleteShift(ShiftOption shift) {
        if (shift == null) return;
        isLoadingLiveData.setValue(true);
        disposables.add(
                deleteShiftUseCase.execute(shift.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoadingLiveData.setValue(false);
                                    ShiftOption currentSelected = selectedShiftLiveData.getValue();
                                    if (currentSelected != null && currentSelected.getId().equals(shift.getId())) {
                                        selectedShiftLiveData.setValue(null);
                                    }
                                    loadShifts();
                                },
                                throwable -> {
                                    isLoadingLiveData.setValue(false);
                                    errorMessageLiveData.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    /**
     * 5. Xác nhận xếp ca cho các nhân viên đã chọn
     */
    public void submitAssignShifts() {
        List<ManagerEmployeeItem> employees = selectedEmployeesLiveData.getValue();
        ShiftOption selectedShift = selectedShiftLiveData.getValue();
        LocalDate selectedDate = selectedDateLiveData.getValue();

        if (employees == null || employees.isEmpty()) {
            errorMessageLiveData.setValue("Danh sách nhân viên không được để trống!");
            return;
        }

        if (selectedShift == null) {
            errorMessageLiveData.setValue("Vui lòng chọn 1 ca làm việc!");
            return;
        }

        isLoadingLiveData.setValue(true);

        for (ManagerEmployeeItem emp : employees) {
            disposables.add(
                    assignShiftUseCase.execute(
                                    emp.getUserId(),
                                    emp.getFullName(),
                                    emp.getPosition(),
                                    selectedShift.getId(),
                                    selectedDate != null ? selectedDate.toString() : LocalDate.now().toString()
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        isLoadingLiveData.setValue(false);
                                        assignSuccessLiveData.setValue(true);
                                    },
                                    throwable -> {
                                        isLoadingLiveData.setValue(false);
                                        errorMessageLiveData.setValue(throwable.getMessage());
                                    }
                            )
            );
        }
    }

    // --- GETTERS & SETTERS ---
    public void selectShift(ShiftOption shift) {
        selectedShiftLiveData.setValue(shift);
    }

    public void setSelectedDate(LocalDate date) {
        selectedDateLiveData.setValue(date);
    }

    public LiveData<List<ManagerEmployeeItem>> getSelectedEmployeesLiveData() { return selectedEmployeesLiveData; }
    public LiveData<List<ShiftOption>> getShiftsLiveData() { return shiftsLiveData; }
    public LiveData<ShiftOption> getSelectedShiftLiveData() { return selectedShiftLiveData; }
    public LiveData<LocalDate> getSelectedDateLiveData() { return selectedDateLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<Boolean> getAssignSuccessLiveData() { return assignSuccessLiveData; }
    public LiveData<String> getErrorMessageLiveData() { return errorMessageLiveData; }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}