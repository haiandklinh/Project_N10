package com.example.attendanceapp.ui.manager.schedule;

import com.example.attendanceapp.domain.model.DayStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ManagerScheduleUiState {
    private final boolean isLoading;
    private final String error;
    private final List<ManagerEmployeeItem> employees;
    private final List<ShiftOption> shiftOptions;
    private final Map<LocalDate, DayStatus> dayStatuses;
    private final boolean isAssignSuccess;

    public ManagerScheduleUiState(boolean isLoading, String error,
                                  List<ManagerEmployeeItem> employees,
                                  List<ShiftOption> shiftOptions,
                                  Map<LocalDate, DayStatus> dayStatuses,
                                  boolean isAssignSuccess) {
        this.isLoading = isLoading;
        this.error = error;
        this.employees = employees;
        this.shiftOptions = shiftOptions;
        this.dayStatuses = dayStatuses;
        this.isAssignSuccess = isAssignSuccess;
    }

    public boolean isLoading() { return isLoading; }
    public String getError() { return error; }
    public List<ManagerEmployeeItem> getEmployees() { return employees; }
    public List<ShiftOption> getShiftOptions() { return shiftOptions; }
    public Map<LocalDate, DayStatus> getDayStatuses() { return dayStatuses; }
    public boolean isAssignSuccess() { return isAssignSuccess; }
}