package com.example.attendanceapp.ui.employee.home;

import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.WeekDay;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public class EmployeeHomeUiState {
    private final String userName;
    private final String companyName;
    private final String avatarUrl;

    private final Shift todayShift;
    private final LocalTime checkInTime;
    private final LocalTime checkOutTime;
    private final List<WeekDay> weekDays;
    private final List<RecentAttendance> recentAttendances;


    private final boolean showNotificationBadge;
    private final boolean isLoading;
    private final String errorMessage;

    public EmployeeHomeUiState(String userName, String companyName, String avatarUrl,
                               Shift todayShift, LocalTime checkInTime, LocalTime checkOutTime,
                               List<WeekDay> weekDays, List<RecentAttendance> recentAttendances,
                               boolean showNotificationBadge, boolean isLoading, String errorMessage) {
        this.userName = userName;
        this.companyName = companyName;
        this.avatarUrl = avatarUrl;
        this.todayShift = todayShift;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.weekDays = weekDays != null ? weekDays : Collections.emptyList();
        this.recentAttendances = recentAttendances != null ? recentAttendances : Collections.emptyList();
        this.showNotificationBadge = showNotificationBadge;
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
    }

    // --- State khởi tạo nhanh trong ViewModel ---
    public static EmployeeHomeUiState loading() {
        return new EmployeeHomeUiState(null, null, null, null, null, null,
                null, null, false, true, null);
    }

    public static EmployeeHomeUiState error(String message) {
        return new EmployeeHomeUiState(null, null, null, null, null, null,
                null, null, false, false, message);
    }

    // TODO: Sử dụng khi tích hợp UseCase - tạo state success từ dữ liệu thực
    public static EmployeeHomeUiState success(String userName, String companyName, String avatarUrl,
                                              Shift todayShift, LocalTime checkInTime, LocalTime checkOutTime,
                                              List<WeekDay> weekDays, List<RecentAttendance> recentAttendances,
                                              boolean showNotificationBadge) {
        return new EmployeeHomeUiState(userName, companyName, avatarUrl, todayShift, checkInTime, checkOutTime, weekDays,
                recentAttendances, showNotificationBadge, false, null);
    }

    // --- Getters ---
    public String getUserName() { return userName; }
    public String getCompanyName() { return companyName; }
    public String getAvatarUrl() { return avatarUrl; }
    public Shift getTodayShift() { return todayShift; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public List<WeekDay> getWeekDays() { return weekDays; }
    public List<RecentAttendance> getRecentAttendances() { return recentAttendances; }
    public boolean shouldShowNotificationBadge() { return showNotificationBadge; }
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
}