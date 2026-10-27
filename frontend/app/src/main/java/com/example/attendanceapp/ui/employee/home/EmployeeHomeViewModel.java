package com.example.attendanceapp.ui.employee.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.data.mapper.StaffScheduleMapper;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.usecase.profile.GetCurrentUserUseCase;
import com.example.attendanceapp.domain.usecase.profile.GetProfileUseCase;
import com.example.attendanceapp.domain.usecase.profile.ObserveCurrentUserUseCase;
import com.example.attendanceapp.domain.usecase.schedule.GetScheduleByDateUseCase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EmployeeHomeViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ObserveCurrentUserUseCase observeCurrentUserUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetScheduleByDateUseCase getScheduleByDateUseCase;
    private final NotificationRepository notificationRepository;
    private final SharedPreferenceApp sharedPreferenceApp;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private boolean hasUnreadNotifications = false;

    // Single Source of Truth chứa toàn bộ UI State của màn hình (luôn khởi tạo sẵn 7 ngày trong tuần)
    private final MutableLiveData<EmployeeHomeUiState> _uiState =
            new MutableLiveData<>(EmployeeHomeUiState.loading(buildEmptyWeekDays(LocalDate.now())));

    public LiveData<EmployeeHomeUiState> getUiState() {
        return _uiState;
    }

    @Inject
    public EmployeeHomeViewModel(
            GetCurrentUserUseCase getCurrentUserUseCase,
            ObserveCurrentUserUseCase observeCurrentUserUseCase,
            GetProfileUseCase getProfileUseCase,
            GetScheduleByDateUseCase getScheduleByDateUseCase,
            NotificationRepository notificationRepository,
            SharedPreferenceApp sharedPreferenceApp
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.observeCurrentUserUseCase = observeCurrentUserUseCase;
        this.getProfileUseCase = getProfileUseCase;
        this.getScheduleByDateUseCase = getScheduleByDateUseCase;
        this.notificationRepository = notificationRepository;
        this.sharedPreferenceApp = sharedPreferenceApp;

        observeCurrentUser();
        observeNotifications();
        loadHomeData();
    }

    // =====================================================================
    // 1. OBSERVERS (STREAM DATA)
    // =====================================================================

    private void observeCurrentUser() {
        disposables.add(observeCurrentUserUseCase.execute()
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
                }, throwable -> { }));
    }

    private void observeNotifications() {
        disposables.add(notificationRepository.observeUnreadCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    this.hasUnreadNotifications = count != null && count > 0;
                    EmployeeHomeUiState current = _uiState.getValue();
                    if (current != null) {
                        _uiState.setValue(new EmployeeHomeUiState(
                                current.getUserName(),
                                current.getCompanyName(),
                                current.getAvatarUrl(),
                                current.getTodayShift(),
                                current.getCheckInTime(),
                                current.getCheckOutTime(),
                                current.getWeekDays(),
                                current.getRecentAttendances(),
                                hasUnreadNotifications,
                                current.isLoading(),
                                current.getErrorMessage()
                        ));
                    }
                }, throwable -> { }));
    }

    public void refreshData() {
        loadHomeData();
    }

    public void onCheckInOrOutClicked() {
        // Chờ backend có endpoint chấm công
    }

    // =====================================================================
    // 2. DATA LOADING & BUSINESS LOGIC
    // =====================================================================

    public void loadHomeData() {
        LocalDate today = LocalDate.now();
        List<WeekDay> existingWeekDays = (_uiState.getValue() != null && !_uiState.getValue().getWeekDays().isEmpty())
                ? _uiState.getValue().getWeekDays()
                : buildEmptyWeekDays(today);

        _uiState.setValue(EmployeeHomeUiState.loading(existingWeekDays));

        // Làm mới hồ sơ từ server ở nền. Lỗi mạng thì bỏ qua, vẫn dùng dữ liệu trong Room.
        disposables.add(getProfileUseCase.execute()
                .subscribeOn(Schedulers.io())
                .subscribe(profile -> { }, throwable -> { }));

        // Làm mới thông báo nền từ server
        disposables.add(notificationRepository.refreshNotifications()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> { }, throwable -> { }));

        Single<UserAccount> userSingle = getCurrentUserUseCase.execute()
                .switchIfEmpty(Single.error(new IllegalStateException("Chưa có thông tin tài khoản")));

        // execute(null): lấy toàn bộ lịch của tôi, tự tính ca hôm nay và lịch tuần từ đó (1 lần gọi mạng).
        Single<List<StaffWorkScheduleResponse>> scheduleSingle = getScheduleByDateUseCase.execute(null);

        disposables.add(Single.zip(userSingle, scheduleSingle, this::buildState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        _uiState::setValue,
                        throwable -> _uiState.setValue(
                                EmployeeHomeUiState.error(throwable.getLocalizedMessage(), existingWeekDays))
                ));
    }

    private EmployeeHomeUiState buildState(UserAccount user, List<StaffWorkScheduleResponse> schedules) {
        LocalDate today = LocalDate.now();
        Shift todayShift = findTodayShift(schedules, today, user.getUserId());
        LocalTime checkInTime = null;
        LocalTime checkOutTime = null;
        if (todayShift != null && sharedPreferenceApp != null) {
            checkInTime = parseTime(sharedPreferenceApp.getShiftCheckInTime(todayShift.getId(), today.toString(), todayShift.getShiftName()));
            checkOutTime = parseTime(sharedPreferenceApp.getShiftCheckOutTime(todayShift.getId(), today.toString(), todayShift.getShiftName()));
        }

        return new EmployeeHomeUiState(
                user.getDisplayName(),
                user.getCompanyName(),
                user.getAvatarUrl(),
                todayShift,
                checkInTime,
                checkOutTime,
                buildWeekDays(schedules, today),
                buildRecentAttendances(schedules, today),
                hasUnreadNotifications, // showNotificationBadge
                false,                 // isLoading
                null                   // errorMessage
        );
    }

    // =====================================================================
    // 3. MAP LỊCH TỪ SERVER SANG DOMAIN
    // =====================================================================

    /** Ca hôm nay: ca đầu tiên chưa kết thúc (theo giờ bắt đầu), nếu đã hết thì lấy ca cuối. Không có ca thì null. */
    private Shift findTodayShift(List<StaffWorkScheduleResponse> schedules, LocalDate today, String employeeId) {
        List<Shift> candidates = new ArrayList<>();

        if (schedules == null) return null;

        for (StaffWorkScheduleResponse item : schedules) {
            if (item == null || item.getShifts() == null) continue;
            if (!today.equals(parseDate(item.getWorkDay()))) continue;

            for (StaffWorkScheduleResponse.ShiftInfo s : item.getShifts()) {
                if (s == null) continue;
                String location = "";
                if (sharedPreferenceApp != null) {
                    location = sharedPreferenceApp.getShiftAddress(s.getShiftName());
                }
                if (location == null || location.trim().isEmpty()) {
                    if (sharedPreferenceApp != null) {
                        location = sharedPreferenceApp.getLatestShiftAddress();
                        if (location == null || location.trim().isEmpty()) {
                            location = sharedPreferenceApp.getCompanyAddress();
                        }
                    }
                }
                if (location == null || location.trim().isEmpty()) {
                    location = "Vị trí ca làm việc";
                }

                LocalTime startTime = parseTime(s.getStartTime());
                LocalTime endTime = parseTime(s.getEndTime());
                LocalTime allowedCheckIn = parseTime(s.getAllowedCheckInTime());
                if (allowedCheckIn == null && sharedPreferenceApp != null) {
                    allowedCheckIn = parseTime(sharedPreferenceApp.getShiftAllowedCheckInTime(s.getShiftName()));
                }
                if (allowedCheckIn == null) {
                    allowedCheckIn = startTime;
                }

                CheckState checkState = CheckState.NOT_CHECKED_IN;
                if (sharedPreferenceApp != null) {
                    checkState = sharedPreferenceApp.getShiftAttendanceState(s.getWorkScheduleId(), item.getWorkDay(), s.getShiftName());
                }

                ShiftStatus status = StaffScheduleMapper.calculateShiftStatus(today, startTime, endTime, checkState);

                candidates.add(new Shift(
                        s.getWorkScheduleId(),
                        employeeId,
                        today,
                        s.getShiftName(),
                        startTime,
                        endTime,
                        location,
                        status,
                        allowedCheckIn
                ));
            }
        }

        if (candidates.isEmpty()) return null;

        candidates.sort(Comparator.comparing(Shift::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        LocalTime now = LocalTime.now();
        for (Shift shift : candidates) {
            if (shift.getEndTime() == null || shift.getEndTime().isAfter(now)) {
                return shift;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    /**
     * Xây dựng danh sách 7 ngày trong tuần với trạng thái chấm công đầy đủ:
     * - Ngày có ca: đã chấm công (PRESENT), vắng mặt nếu quá hạn (ABSENT), chưa chấm (NOT_CHECKED)
     * - Ngày không có ca: cuối tuần (WEEKEND), ngày thường không có ca (LEAVE - nghỉ phép)
     */
    private List<WeekDay> buildWeekDays(List<StaffWorkScheduleResponse> schedules, LocalDate today) {
        Set<LocalDate> scheduledDates = new HashSet<>();
        Map<LocalDate, Boolean> hasAttendanceMap = new HashMap<>();

        if (schedules != null) {
            for (StaffWorkScheduleResponse item : schedules) {
                if (item == null) continue;
                LocalDate d = parseDate(item.getWorkDay());
                if (d == null) continue;

                if (item.getShifts() != null && !item.getShifts().isEmpty()) {
                    scheduledDates.add(d);
                    for (StaffWorkScheduleResponse.ShiftInfo s : item.getShifts()) {
                        if (s == null) continue;
                        if (sharedPreferenceApp != null) {
                            CheckState cs = sharedPreferenceApp.getShiftAttendanceState(
                                    s.getWorkScheduleId(), item.getWorkDay(), s.getShiftName()
                            );
                            if (cs == CheckState.CHECKED_IN || cs == CheckState.CHECKED_OUT) {
                                hasAttendanceMap.put(d, true);
                            }
                        }
                    }
                }
            }
        }

        List<WeekDay> weekDays = new ArrayList<>();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
            boolean hasShift = scheduledDates.contains(date);
            boolean hasAttendance = Boolean.TRUE.equals(hasAttendanceMap.get(date));

            AttendanceStatus status;
            if (hasShift) {
                if (hasAttendance) {
                    status = AttendanceStatus.PRESENT;
                } else if (date.isBefore(today)) {
                    status = AttendanceStatus.ABSENT;
                } else {
                    status = AttendanceStatus.NOT_CHECKED;
                }
            } else {
                if (isWeekend) {
                    status = AttendanceStatus.WEEKEND;
                } else {
                    // Ngày thường không có ca làm việc -> Trạng thái nghỉ phép
                    status = AttendanceStatus.LEAVE;
                }
            }

            weekDays.add(new WeekDay(date, status, date.equals(today)));
        }
        return weekDays;
    }

    private static List<WeekDay> buildEmptyWeekDays(LocalDate today) {
        List<WeekDay> weekDays = new ArrayList<>();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
            AttendanceStatus status = isWeekend ? AttendanceStatus.WEEKEND : AttendanceStatus.LEAVE;
            weekDays.add(new WeekDay(date, status, date.equals(today)));
        }
        return weekDays;
    }

    /**
     * Xây dựng danh sách chấm công gần đây:
     * - Khi một ca làm việc của nhân viên kết thúc (hoặc các ca trước đó), tạo bản ghi RecentAttendance
     * - Hiển thị trạng thái: Đúng giờ (PRESENT), Đi muộn (LATE), Về sớm (EARLY_LEAVE), Vắng mặt (ABSENT).
     */
    private List<RecentAttendance> buildRecentAttendances(List<StaffWorkScheduleResponse> schedules, LocalDate today) {
        List<RecentAttendance> recentList = new ArrayList<>();
        if (schedules == null) return recentList;

        LocalTime now = LocalTime.now();

        for (StaffWorkScheduleResponse schedule : schedules) {
            if (schedule == null || schedule.getWorkDay() == null) continue;
            LocalDate workDate = parseDate(schedule.getWorkDay());
            if (workDate == null) continue;

            // Chỉ lấy các ngày từ hôm nay trở về trước (không lấy tương lai)
            if (workDate.isAfter(today)) continue;

            if (schedule.getShifts() == null) continue;

            for (StaffWorkScheduleResponse.ShiftInfo shift : schedule.getShifts()) {
                if (shift == null) continue;

                LocalTime startTime = parseTime(shift.getStartTime());
                LocalTime endTime = parseTime(shift.getEndTime());

                CheckState checkState = CheckState.NOT_CHECKED_IN;
                LocalTime checkIn = null;
                LocalTime checkOut = null;

                if (sharedPreferenceApp != null) {
                    checkState = sharedPreferenceApp.getShiftAttendanceState(
                            shift.getWorkScheduleId(), schedule.getWorkDay(), shift.getShiftName()
                    );
                    String inStr = sharedPreferenceApp.getShiftCheckInTime(
                            shift.getWorkScheduleId(), schedule.getWorkDay(), shift.getShiftName()
                    );
                    if (inStr != null) checkIn = parseTime(inStr);

                    String outStr = sharedPreferenceApp.getShiftCheckOutTime(
                            shift.getWorkScheduleId(), schedule.getWorkDay(), shift.getShiftName()
                    );
                    if (outStr != null) checkOut = parseTime(outStr);
                }

                boolean isEnded;
                if (workDate.isBefore(today)) {
                    isEnded = true;
                } else {
                    // workDate is today:
                    // Ca kết thúc nếu đã quá endTime HOẶC nhân viên đã checkout
                    boolean pastEndTime = (endTime != null && now.isAfter(endTime));
                    boolean checkedOut = (checkOut != null || checkState == CheckState.CHECKED_OUT);
                    isEnded = pastEndTime || checkedOut;
                }

                if (isEnded) {
                    AttendanceStatus status;
                    if (checkIn != null) {
                        if (startTime != null && checkIn.isAfter(startTime)) {
                            status = AttendanceStatus.LATE;
                        } else if (checkOut != null && endTime != null && checkOut.isBefore(endTime)) {
                            status = AttendanceStatus.EARLY_LEAVE;
                        } else {
                            status = AttendanceStatus.PRESENT;
                        }
                    } else if (checkState == CheckState.CHECKED_IN || checkState == CheckState.CHECKED_OUT) {
                        status = AttendanceStatus.PRESENT;
                    } else {
                        // Ca đã kết thúc mà nhân viên chưa check-in -> Vắng mặt
                        status = AttendanceStatus.ABSENT;
                    }

                    recentList.add(new RecentAttendance(
                            workDate,
                            checkIn,
                            checkOut,
                            status
                    ));
                }
            }
        }

        // Sắp xếp ca gần đây nhất lên đầu (theo ngày giảm dần, sau đó theo giờ check-in giảm dần)
        recentList.sort((a, b) -> {
            int cmp = b.getDate().compareTo(a.getDate());
            if (cmp != 0) return cmp;
            LocalTime tA = a.getCheckIn() != null ? a.getCheckIn() : LocalTime.MIN;
            LocalTime tB = b.getCheckIn() != null ? b.getCheckIn() : LocalTime.MIN;
            return tB.compareTo(tA);
        });

        // Giới hạn hiển thị 5 ca gần đây nhất
        if (recentList.size() > 5) {
            return new ArrayList<>(recentList.subList(0, 5));
        }
        return recentList;
    }

    private static LocalDate parseDate(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime parseTime(String s) {
        if (s == null) return null;
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
