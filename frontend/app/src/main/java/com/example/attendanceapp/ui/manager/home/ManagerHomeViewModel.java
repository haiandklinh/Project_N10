package com.example.attendanceapp.ui.manager.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordItemResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.local.HandledRequestManager;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.RecentCheckInItem;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.AttendanceRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.repository.ScheduleRepository;
import com.example.attendanceapp.domain.usecase.home.GetTodayShiftUseCase;
import com.example.attendanceapp.domain.usecase.profile.GetCurrentUserUseCase;
import com.example.attendanceapp.domain.usecase.profile.GetProfileUseCase;
import com.example.attendanceapp.domain.usecase.profile.ObserveCurrentUserUseCase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerHomeViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ObserveCurrentUserUseCase observeCurrentUserUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final NotificationRepository notificationRepository;
    private final ManagerRepository managerRepository;
    private final HandledRequestManager handledRequestManager;
    private final GetTodayShiftUseCase getTodayShiftUseCase;
    private final ScheduleRepository scheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final SharedPreferenceApp sharedPreferenceApp;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private boolean hasUnreadNotifications = false;
    private String userName = "";
    private String companyName = "";
    private String companyId = "";
    private String avatarUrl = "";
    private int totalEmployees = 0;
    private int presentCount = 0;
    private int lateCount = 0;
    private int absentCount = 0;
    private int leaveCount = 0;
    private int pendingApprovalCount = 0;
    private boolean isLoading = false;
    private String errorMessage = null;
    private List<TodayShiftSummary> todayShifts = new ArrayList<>();
    private List<RecentCheckInItem> recentCheckIns = new ArrayList<>();

    private List<EmployeeWorkSchedule> rawSchedules = new ArrayList<>();
    private List<AttendanceRecordItemResponse> todayServerRecords = new ArrayList<>();

    private final MutableLiveData<ManagerHomeUiState> _uiState = new MutableLiveData<>(ManagerHomeUiState.loading());

    public LiveData<ManagerHomeUiState> getUiState() {
        return _uiState;
    }

    @Inject
    public ManagerHomeViewModel(
            GetCurrentUserUseCase getCurrentUserUseCase,
            ObserveCurrentUserUseCase observeCurrentUserUseCase,
            GetProfileUseCase getProfileUseCase,
            NotificationRepository notificationRepository,
            ManagerRepository managerRepository,
            HandledRequestManager handledRequestManager,
            WebSocketNotificationManager webSocketManager,
            GetTodayShiftUseCase getTodayShiftUseCase,
            ScheduleRepository scheduleRepository,
            AttendanceRepository attendanceRepository,
            SharedPreferenceApp sharedPreferenceApp
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.observeCurrentUserUseCase = observeCurrentUserUseCase;
        this.getProfileUseCase = getProfileUseCase;
        this.notificationRepository = notificationRepository;
        this.managerRepository = managerRepository;
        this.handledRequestManager = handledRequestManager;
        this.getTodayShiftUseCase = getTodayShiftUseCase;
        this.scheduleRepository = scheduleRepository;
        this.attendanceRepository = attendanceRepository;
        this.sharedPreferenceApp = sharedPreferenceApp;

        observeUserAccount();
        observeNotifications();
        observeHandledRequests();
        observeTodayShifts();
        observeScheduleAndAttendances();

        disposables.add(webSocketManager.observeIncomingNotifications()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notification -> {
                    loadApprovalRequests();
                    loadEmployeeCount();
                    loadTodayAttendanceRecords();
                }, throwable -> {}));

        loadHomeData();
    }

    private void observeHandledRequests() {
        disposables.add(handledRequestManager.observePendingRequestsChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> {
                    loadApprovalRequests();
                    loadEmployeeCount();
                }, throwable -> {}));

        disposables.add(handledRequestManager.observeEmployees()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    loadEmployeeCount();
                }, throwable -> {}));
    }

    private void observeTodayShifts() {
        disposables.add(getTodayShiftUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shifts -> {
                    this.todayShifts = (shifts != null) ? shifts : new ArrayList<>();
                    emitCurrentState();
                }, throwable -> { }));
    }

    private void observeScheduleAndAttendances() {
        disposables.add(scheduleRepository.observeEmployeeWorkSchedules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(schedules -> {
                    this.rawSchedules = (schedules != null) ? schedules : new ArrayList<>();
                    calculateAttendanceSummaryAndRecentCheckins();
                }, throwable -> { }));
    }

    private void observeUserAccount() {
        disposables.add(observeCurrentUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    if (user != null) {
                        this.userName = user.getDisplayName();
                        this.companyName = user.getCompanyName();
                        this.companyId = user.getCompanyId() != null ? user.getCompanyId() : "";
                        this.avatarUrl = user.getAvatarUrl();
                        emitCurrentState();
                    }
                }, throwable -> { }));
    }

    private void observeNotifications() {
        disposables.add(notificationRepository.observeUnreadCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    this.hasUnreadNotifications = count != null && count > 0;
                    emitCurrentState();
                }, throwable -> { }));
    }

    public void loadHomeData() {
        this.isLoading = true;
        emitCurrentState();

        // 1. Làm mới hồ sơ từ server ở nền
        disposables.add(getProfileUseCase.execute()
                .subscribeOn(Schedulers.io())
                .subscribe(profile -> { }, throwable -> { }));

        // 2. Làm mới thông báo từ server
        disposables.add(notificationRepository.refreshNotifications()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> { }, throwable -> { }));

        // 3. Lấy thông tin user hiện tại
        disposables.add(getCurrentUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            this.userName = user.getDisplayName();
                            this.companyName = user.getCompanyName();
                            this.companyId = user.getCompanyId() != null ? user.getCompanyId() : "";
                            this.avatarUrl = user.getAvatarUrl();
                            this.isLoading = false;
                            emitCurrentState();
                            loadTodayAttendanceRecords();
                        },
                        throwable -> {
                            this.isLoading = false;
                            this.errorMessage = throwable.getLocalizedMessage();
                            emitCurrentState();
                        },
                        () -> {
                            this.isLoading = false;
                            emitCurrentState();
                        }
                ));

        // 4. Lấy số lượng nhân viên thực tế của công ty
        loadEmployeeCount();

        // 5. Lấy danh sách yêu cầu cần duyệt thực tế
        loadApprovalRequests();

        // 6. Làm mới lịch làm việc và danh sách ca từ server
        disposables.add(getTodayShiftUseCase.refresh()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> { }, throwable -> { }));

        // 7. Tải lại lịch làm việc để cập nhật Room
        disposables.add(scheduleRepository.getEmployeeWorkSchedules()
                .subscribeOn(Schedulers.io())
                .subscribe(schedules -> { }, throwable -> { }));
    }

    private void loadTodayAttendanceRecords() {
        if (companyId == null || companyId.trim().isEmpty()) return;
        String todayStr = LocalDate.now().toString();
        disposables.add(attendanceRepository.getAttendanceRecords(companyId, todayStr, todayStr, 1, 100)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (resp != null && resp.getData() != null) {
                        this.todayServerRecords = resp.getData();
                        calculateAttendanceSummaryAndRecentCheckins();
                    }
                }, throwable -> { }));
    }

    private void loadEmployeeCount() {
        disposables.add(managerRepository.getStaffs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            int count = 0;
                            if (response != null && response.getData() != null) {
                                count = response.getData().size();
                            }
                            this.totalEmployees = count;
                            emitCurrentState();
                        },
                        throwable -> {
                            int localApproved = handledRequestManager != null ? handledRequestManager.getApprovedEmployeeCount() : 0;
                            this.totalEmployees = localApproved;
                            emitCurrentState();
                        }
                ));
    }

    private void loadApprovalRequests() {
        disposables.add(managerRepository.getNotifications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            int count = 0;
                            if (response != null && response.getData() != null) {
                                for (NotificationItemResponse item : response.getData()) {
                                    String staffId = item.getStaffId();
                                    if (staffId != null && handledRequestManager != null && handledRequestManager.isHandled(staffId)) {
                                        continue;
                                    }

                                    String type = item.getNotificationType();
                                    if (type != null && !type.contains("JOIN") && (
                                            type.contains("CHANGE") ||
                                            type.contains("LEAVE") ||
                                            type.contains("ADJUSTMENT") ||
                                            type.contains("SHIFT") ||
                                            type.contains("REQUEST"))) {
                                        count++;
                                    }
                                }
                            }
                            this.pendingApprovalCount = count;
                            emitCurrentState();
                        },
                        throwable -> {
                            this.pendingApprovalCount = 0;
                            emitCurrentState();
                        }
                ));
    }

    /**
     * Tính toán danh sách "Check-in hôm nay" và các chỉ số thống kê (Có mặt, Đi muộn, Vắng mặt)
     * - Khi một ca làm việc kết thúc, toàn bộ nhân viên được xếp ca sẽ hiển thị kết quả chấm công
     * - Nhân viên đã check-in: Đúng giờ (PRESENT), Đi muộn (LATE), Về sớm (EARLY_LEAVE)
     * - Nhân viên không check-in khi ca đã kết thúc: Vắng mặt (ABSENT)
     */
    private void calculateAttendanceSummaryAndRecentCheckins() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<RecentCheckInItem> checkInList = new ArrayList<>();
        int present = 0;
        int late = 0;
        int absent = 0;
        int leave = 0;

        List<EmployeeWorkSchedule> todayEmpSchedules = new ArrayList<>();
        if (rawSchedules != null) {
            for (EmployeeWorkSchedule s : rawSchedules) {
                if (s != null && s.getShift() != null && isDateMatch(s.getWorkDate(), today)) {
                    todayEmpSchedules.add(s);
                }
            }
        }

        for (EmployeeWorkSchedule schedule : todayEmpSchedules) {
            EmployeeWorkSchedule.ShiftInfo shift = schedule.getShift();
            String shiftName = (shift.getShiftName() != null && !shift.getShiftName().trim().isEmpty())
                    ? shift.getShiftName().trim() : "Ca làm việc";
            LocalTime startTime = parseTime(shift.getStartTime());
            LocalTime endTime = parseTime(shift.getEndTime());

            // 1. Đối chiếu bản ghi từ server
            AttendanceRecordItemResponse matchedRecord = null;
            if (todayServerRecords != null) {
                for (AttendanceRecordItemResponse rec : todayServerRecords) {
                    if (rec == null) continue;
                    boolean matchShift = shiftName.equalsIgnoreCase(rec.getShiftName());
                    boolean matchStaff = (schedule.getEmployeeId() != null && schedule.getEmployeeId().equals(rec.getStaffId()))
                            || (schedule.getName() != null && schedule.getName().equalsIgnoreCase(rec.getStaffName()));

                    if (matchStaff && matchShift) {
                        matchedRecord = rec;
                        break;
                    } else if (matchShift && matchedRecord == null) {
                        matchedRecord = rec;
                    }
                }
            }

            // 2. Đối chiếu bản ghi cục bộ (SharedPreferenceApp)
            String localIn = null;
            String localOut = null;
            CheckState localState = CheckState.NOT_CHECKED_IN;
            if (sharedPreferenceApp != null) {
                localState = sharedPreferenceApp.getShiftAttendanceState(
                        shift.getShiftId(), today.toString(), shiftName
                );
                localIn = sharedPreferenceApp.getShiftCheckInTime(
                        shift.getShiftId(), today.toString(), shiftName
                );
                localOut = sharedPreferenceApp.getShiftCheckOutTime(
                        shift.getShiftId(), today.toString(), shiftName
                );
            }

            LocalTime checkIn = matchedRecord != null ? parseTime(matchedRecord.getCheckInTimeString()) : null;
            if (checkIn == null && localIn != null) {
                checkIn = parseTime(localIn);
            }

            LocalTime checkOut = matchedRecord != null ? parseTime(matchedRecord.getCheckOutTimeString()) : null;
            if (checkOut == null && localOut != null) {
                checkOut = parseTime(localOut);
            }

            boolean isShiftEnded = (endTime != null && now.isAfter(endTime));

            AttendanceStatus status;
            String timeDisplay;

            if (checkIn != null) {
                timeDisplay = DateTimeFormatUtils.formatTime(checkIn);
                if (startTime != null && checkIn.isAfter(startTime)) {
                    status = AttendanceStatus.LATE;
                    late++;
                } else if (checkOut != null && endTime != null && checkOut.isBefore(endTime)) {
                    status = AttendanceStatus.EARLY_LEAVE;
                    present++;
                } else {
                    status = AttendanceStatus.PRESENT;
                    present++;
                }
            } else if (localState == CheckState.CHECKED_IN || localState == CheckState.CHECKED_OUT) {
                status = AttendanceStatus.PRESENT;
                timeDisplay = (startTime != null) ? DateTimeFormatUtils.formatTime(startTime) : "--:--";
                present++;
            } else {
                if (isShiftEnded) {
                    // Ca đã kết thúc mà nhân viên không chấm công -> Vắng mặt
                    status = AttendanceStatus.ABSENT;
                    timeDisplay = "Chưa chấm công";
                    absent++;
                } else if (startTime != null && now.isAfter(startTime)) {
                    // Đã quá giờ bắt đầu ca mà chưa check-in -> Đi muộn
                    status = AttendanceStatus.LATE;
                    timeDisplay = "Chưa check-in";
                    late++;
                } else {
                    // Ca chưa bắt đầu
                    status = AttendanceStatus.NOT_CHECKED;
                    timeDisplay = "Chưa vào ca";
                }
            }

            // Hiển thị nhân viên trong phần "Check-in hôm nay" nếu:
            // Ca đã kết thúc HOẶC nhân viên đã chấm công / đi muộn / về sớm
            if (isShiftEnded || status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE || status == AttendanceStatus.EARLY_LEAVE) {
                checkInList.add(new RecentCheckInItem(
                        schedule.getEmployeeId() != null ? schedule.getEmployeeId() : "",
                        schedule.getName() != null ? schedule.getName() : "Nhân viên",
                        schedule.getAvatar(),
                        shiftName,
                        timeDisplay,
                        status
                ));
            }
        }

        this.recentCheckIns = checkInList;
        this.presentCount = present;
        this.lateCount = late;
        this.absentCount = absent;
        this.leaveCount = leave;

        emitCurrentState();
    }

    private static boolean isDateMatch(String dateStr, LocalDate targetDate) {
        if (dateStr == null || dateStr.trim().isEmpty() || targetDate == null) return false;
        String clean = dateStr.trim();
        if (clean.equals(targetDate.toString())) return true;
        try {
            if (LocalDate.parse(clean).equals(targetDate)) return true;
        } catch (Exception ignored) {}
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (LocalDate.parse(clean, dtf).equals(targetDate)) return true;
        } catch (Exception ignored) {}
        try {
            DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("d/M/yyyy");
            if (LocalDate.parse(clean, dtf2).equals(targetDate)) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private static LocalTime parseTime(Object obj) {
        if (obj == null) return null;
        String s = obj.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            try {
                if (s.length() >= 5) {
                    return LocalTime.parse(s.substring(0, 5));
                }
            } catch (Exception ignored) {}
            return null;
        }
    }

    private void emitCurrentState() {
        String todayDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now());
        _uiState.setValue(new ManagerHomeUiState(
                userName,
                companyName,
                avatarUrl,
                hasUnreadNotifications,
                isLoading,
                errorMessage,
                todayDate,
                totalEmployees,
                presentCount,
                lateCount,
                absentCount,
                leaveCount,
                pendingApprovalCount,
                todayShifts,
                recentCheckIns
        ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public static class ManagerHomeUiState {
        private final String userName;
        private final String companyName;
        private final String avatarUrl;
        private final boolean hasUnreadNotifications;
        private final boolean isLoading;
        private final String errorMessage;
        private final String todayDate;
        private final int totalEmployees;
        private final int presentCount;
        private final int lateCount;
        private final int absentCount;
        private final int leaveCount;
        private final int pendingApprovalCount;
        private final List<TodayShiftSummary> todayShifts;
        private final List<RecentCheckInItem> recentCheckIns;

        public ManagerHomeUiState(String userName, String companyName, String avatarUrl,
                                  boolean hasUnreadNotifications, boolean isLoading, String errorMessage,
                                  String todayDate, int totalEmployees, int presentCount, int lateCount,
                                  int absentCount, int leaveCount, int pendingApprovalCount,
                                  List<TodayShiftSummary> todayShifts,
                                  List<RecentCheckInItem> recentCheckIns) {
            this.userName = userName;
            this.companyName = companyName;
            this.avatarUrl = avatarUrl;
            this.hasUnreadNotifications = hasUnreadNotifications;
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
            this.todayDate = todayDate;
            this.totalEmployees = totalEmployees;
            this.presentCount = presentCount;
            this.lateCount = lateCount;
            this.absentCount = absentCount;
            this.leaveCount = leaveCount;
            this.pendingApprovalCount = pendingApprovalCount;
            this.todayShifts = todayShifts != null ? todayShifts : Collections.emptyList();
            this.recentCheckIns = recentCheckIns != null ? recentCheckIns : Collections.emptyList();
        }

        public static ManagerHomeUiState loading() {
            String today = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now());
            return new ManagerHomeUiState(null, null, null, false, true, null,
                    today, 0, 0, 0, 0, 0, 0, Collections.emptyList(), Collections.emptyList());
        }

        public static ManagerHomeUiState error(String errorMessage) {
            String today = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now());
            return new ManagerHomeUiState(null, null, null, false, false, errorMessage,
                    today, 0, 0, 0, 0, 0, 0, Collections.emptyList(), Collections.emptyList());
        }

        public String getUserName() { return userName; }
        public String getCompanyName() { return companyName; }
        public String getAvatarUrl() { return avatarUrl; }
        public boolean hasUnreadNotifications() { return hasUnreadNotifications; }
        public boolean isLoading() { return isLoading; }
        public String getErrorMessage() { return errorMessage; }
        public String getTodayDate() { return todayDate; }
        public int getTotalEmployees() { return totalEmployees; }
        public int getPresentCount() { return presentCount; }
        public int getLateCount() { return lateCount; }
        public int getAbsentCount() { return absentCount; }
        public int getLeaveCount() { return leaveCount; }
        public int getPendingApprovalCount() { return pendingApprovalCount; }
        public List<TodayShiftSummary> getTodayShifts() { return todayShifts; }
        public List<RecentCheckInItem> getRecentCheckIns() { return recentCheckIns; }
    }
}
