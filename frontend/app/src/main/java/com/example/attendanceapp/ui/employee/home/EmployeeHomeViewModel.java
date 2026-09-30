package com.example.attendanceapp.ui.employee.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.domain.repository.UserRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EmployeeHomeViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // Single Source of Truth chứa toàn bộ UI State của màn hình
    private final MutableLiveData<EmployeeHomeUiState> _uiState = new MutableLiveData<>(EmployeeHomeUiState.loading());

    public LiveData<EmployeeHomeUiState> getUiState() {
        return _uiState;
    }

    @Inject
    public EmployeeHomeViewModel(
            UserRepository userRepository
            /* TODO: Inject các UseCase/Repository từ Domain Layer tại đây
             * GetTodayShiftUseCase getTodayShiftUseCase,
             * GetWeeklyAttendanceUseCase getWeeklyAttendanceUseCase
             */
    ) {
        this.userRepository = userRepository;
        loadHomeData();
        observeUserAccount();
    }

    private void observeUserAccount() {
        disposables.add(userRepository.observeCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    EmployeeHomeUiState current = _uiState.getValue();
                    if (current != null) {
                        _uiState.setValue(new EmployeeHomeUiState(
                                user.getDisplayName(),
                                user.getCompanyName(),
                                user.getAvatarUrl(),
                                current.getTodayShift(),
                                current.getCheckInTime(),
                                current.getCheckOutTime(),
                                current.getWeekDays(),
                                current.getRecentAttendances(),
                                current.shouldShowNotificationBadge(),
                                current.isLoading(),
                                current.getErrorMessage()
                        ));
                    }
                }, throwable -> {
                    // Handle error if needed
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    // =====================================================================
    // 1. INTENTS / USER ACTIONS (NHẬN SỰ KIỆN TỪ UI)
    // =====================================================================

    /**
     * Tải lại dữ liệu (dùng cho SwipeToRefresh hoặc thử lại khi lỗi)
     */
    public void refreshData() {
        loadHomeData();
    }

    /**
     * Xử lý khi bấm nút Check-in / Check-out
     */
    public void onCheckInOrOutClicked() {
        // TODO: Gọi CheckInUseCase / CheckOutUseCase
    }

    // =====================================================================
    // 2. DATA LOADING & BUSINESS LOGIC
    // =====================================================================

    public void loadHomeData() {
        // Đặt trạng thái Loading ban đầu
        _uiState.setValue(EmployeeHomeUiState.loading());

        disposables.add(userRepository.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    // Trong thực tế sẽ gọi UseCase (Async)
                    LocalTime checkIn = LocalTime.of(8, 2);
                    LocalTime checkOut = null;

                    Shift mockShift = createMockShiftToday();
                    List<WeekDay> weekDays = createMockWeekDays();
                    List<RecentAttendance> recentAttendances = createMockRecentAttendances();

                    // Emit State thành công về Main Thread
                    _uiState.setValue(new EmployeeHomeUiState(
                            user.getDisplayName(),
                            user.getCompanyName(),
                            user.getAvatarUrl(),
                            mockShift,
                            checkIn,
                            checkOut,
                            weekDays,
                            recentAttendances,
                            true,  // showNotificationBadge
                            false, // isLoading
                            null   // errorMessage
                    ));
                }, throwable -> {
                    _uiState.setValue(EmployeeHomeUiState.error(throwable.getLocalizedMessage()));
                }));
    }

    // =====================================================================
    // 3. MOCK DATA GENERATORS (SẼ THAY BẰNG USE CASES)
    // =====================================================================

    private Shift createMockShiftToday() {
        return new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING
        );
    }

    private List<WeekDay> createMockWeekDays() {
        List<WeekDay> weekDays = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            AttendanceStatus status = date.isAfter(today) ? AttendanceStatus.NOT_CHECKED : AttendanceStatus.PRESENT;
            weekDays.add(new WeekDay(date, status, date.equals(today)));
        }
        return weekDays;
    }

    private List<RecentAttendance> createMockRecentAttendances() {
        List<RecentAttendance> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        list.add(new RecentAttendance(today, LocalTime.of(8, 1), LocalTime.of(17, 32), AttendanceStatus.PRESENT));
        list.add(new RecentAttendance(today.minusDays(1), LocalTime.of(8, 5), LocalTime.of(17, 40), AttendanceStatus.ABSENT));
        list.add(new RecentAttendance(today.minusDays(2), LocalTime.of(8, 25), LocalTime.of(17, 30), AttendanceStatus.WEEKEND));
        list.add(new RecentAttendance(today.minusDays(3), LocalTime.of(7, 58), LocalTime.of(17, 35), AttendanceStatus.NOT_CHECKED));
        list.add(new RecentAttendance(today.minusDays(4), LocalTime.of(8, 1), LocalTime.of(17, 32), AttendanceStatus.LATE));

        return list;
    }
}