package com.example.attendanceapp.ui.manager.attendance_history;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordItemResponse;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordPageResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.ManagerAttendanceHistoryItem;
import com.example.attendanceapp.domain.repository.AttendanceRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.ScheduleRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerAttendanceHistoryViewModel extends ViewModel {

    public enum PeriodMode {
        WEEK,
        MONTH
    }

    public static class SummaryStats {
        public final int presentCount;
        public final int lateCount;
        public final int absentCount;
        public final int earlyLeaveCount;

        public SummaryStats(int presentCount, int lateCount, int absentCount, int earlyLeaveCount) {
            this.presentCount = presentCount;
            this.lateCount = lateCount;
            this.absentCount = absentCount;
            this.earlyLeaveCount = earlyLeaveCount;
        }
    }

    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;
    private final SessionRepository sessionRepository;
    private final ManagerRepository managerRepository;
    private final SharedPreferenceApp sharedPreferenceApp;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private PeriodMode currentMode = PeriodMode.WEEK;
    private LocalDate currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private YearMonth currentMonth = YearMonth.now();
    @Nullable
    private AttendanceStatus currentFilter = null;

    private final List<ManagerAttendanceHistoryItem> allItems = new ArrayList<>();

    private final MutableLiveData<PeriodMode> _periodMode = new MutableLiveData<>(PeriodMode.WEEK);
    private final MutableLiveData<String> _periodTitle = new MutableLiveData<>();
    private final MutableLiveData<List<ManagerAttendanceHistoryItem>> _attendanceList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<SummaryStats> _summaryStats = new MutableLiveData<>(new SummaryStats(0, 0, 0, 0));
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    public LiveData<PeriodMode> getPeriodMode() { return _periodMode; }
    public LiveData<String> getPeriodTitle() { return _periodTitle; }
    public LiveData<List<ManagerAttendanceHistoryItem>> getAttendanceList() { return _attendanceList; }
    public LiveData<SummaryStats> getSummaryStats() { return _summaryStats; }
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    @Inject
    public ManagerAttendanceHistoryViewModel(
            AttendanceRepository attendanceRepository,
            ScheduleRepository scheduleRepository,
            SessionRepository sessionRepository,
            ManagerRepository managerRepository,
            SharedPreferenceApp sharedPreferenceApp
    ) {
        this.attendanceRepository = attendanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.sessionRepository = sessionRepository;
        this.managerRepository = managerRepository;
        this.sharedPreferenceApp = sharedPreferenceApp;

        updatePeriodTitle();
        loadAttendanceHistory();
    }

    public void setPeriodMode(PeriodMode mode) {
        if (this.currentMode != mode) {
            this.currentMode = mode;
            _periodMode.setValue(mode);
            updatePeriodTitle();
            loadAttendanceHistory();
        }
    }

    public void previousPeriod() {
        if (currentMode == PeriodMode.WEEK) {
            currentWeekMonday = currentWeekMonday.minusWeeks(1);
        } else {
            currentMonth = currentMonth.minusMonths(1);
        }
        updatePeriodTitle();
        loadAttendanceHistory();
    }

    public void nextPeriod() {
        if (currentMode == PeriodMode.WEEK) {
            currentWeekMonday = currentWeekMonday.plusWeeks(1);
        } else {
            currentMonth = currentMonth.plusMonths(1);
        }
        updatePeriodTitle();
        loadAttendanceHistory();
    }

    public void setFilter(@Nullable AttendanceStatus filter) {
        this.currentFilter = filter;
        applyFilterAndUpdate();
    }

    private void updatePeriodTitle() {
        if (currentMode == PeriodMode.WEEK) {
            LocalDate sunday = currentWeekMonday.plusDays(6);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            String title = "Tuần " + currentWeekMonday.format(formatter) + " - " + sunday.format(formatter) + "/" + currentWeekMonday.getYear();
            _periodTitle.setValue(title);
        } else {
            String title = "Tháng " + currentMonth.getMonthValue() + "/" + currentMonth.getYear();
            _periodTitle.setValue(title);
        }
    }

    public void loadAttendanceHistory() {
        _isLoading.setValue(true);

        LocalDate startDate;
        LocalDate endDate;
        if (currentMode == PeriodMode.WEEK) {
            startDate = currentWeekMonday;
            endDate = currentWeekMonday.plusDays(6);
        } else {
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        }

        String startStr = startDate.toString();
        String endStr = endDate.toString();

        Single<String> companyIdSingle = sessionRepository.getCurrentUser()
                .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                .defaultIfEmpty("")
                .subscribeOn(Schedulers.io());

        Single<AttendanceRecordPageResponse> attendanceSingle = companyIdSingle.flatMap(companyId -> {
            if (companyId.isEmpty()) {
                return Single.just(new AttendanceRecordPageResponse());
            }
            return attendanceRepository.getAttendanceRecords(companyId, startStr, endStr, 1, 500)
                    .onErrorReturnItem(new AttendanceRecordPageResponse());
        }).onErrorReturnItem(new AttendanceRecordPageResponse());

        Single<List<EmployeeWorkSchedule>> schedulesSingle = scheduleRepository.getEmployeeWorkSchedules()
                .onErrorReturnItem(new ArrayList<>());

        Single<List<StaffItemResponse>> staffsSingle = managerRepository.getStaffs()
                .map(resp -> resp.getData() != null ? resp.getData() : new ArrayList<StaffItemResponse>())
                .onErrorReturnItem(new ArrayList<>());

        disposables.add(Single.zip(attendanceSingle, schedulesSingle, staffsSingle, (attPage, schedules, staffs) ->
                        processRecords(attPage, schedules, staffs, startDate, endDate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    _isLoading.setValue(false);
                    allItems.clear();
                    if (items != null && !items.isEmpty()) {
                        allItems.addAll(items);
                    } else {
                        allItems.addAll(generateSampleData(startDate, endDate));
                    }
                    applyFilterAndUpdate();
                    updateStats();
                }, throwable -> {
                    _isLoading.setValue(false);
                    allItems.clear();
                    allItems.addAll(generateSampleData(startDate, endDate));
                    applyFilterAndUpdate();
                    updateStats();
                })
        );
    }

    private List<ManagerAttendanceHistoryItem> processRecords(
            AttendanceRecordPageResponse attPage,
            List<EmployeeWorkSchedule> schedules,
            List<StaffItemResponse> staffs,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ManagerAttendanceHistoryItem> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Map<String, StaffItemResponse> staffMap = new HashMap<>();
        if (staffs != null) {
            for (StaffItemResponse s : staffs) {
                if (s.getStaffId() != null) staffMap.put(s.getStaffId(), s);
                if (s.getNameStaff() != null) staffMap.put(s.getNameStaff().toLowerCase(Locale.ROOT), s);
            }
        }

        List<AttendanceRecordItemResponse> serverRecords = (attPage != null && attPage.getData() != null)
                ? attPage.getData() : Collections.emptyList();
        Set<AttendanceRecordItemResponse> matchedServerRecords = new HashSet<>();

        // 1. Đối chiếu từ danh sách ca làm việc của toàn bộ nhân viên (schedules)
        if (schedules != null) {
            for (EmployeeWorkSchedule schedule : schedules) {
                if (schedule == null || schedule.getWorkDate() == null) continue;
                LocalDate scheduleDate = parseDate(schedule.getWorkDate());
                if (scheduleDate == null) continue;
                if (scheduleDate.isBefore(startDate) || scheduleDate.isAfter(endDate)) continue;

                EmployeeWorkSchedule.ShiftInfo shift = schedule.getShift();
                String shiftName = (shift != null && shift.getShiftName() != null) ? shift.getShiftName().trim() : "Ca làm việc";
                LocalTime startTime = shift != null ? parseTime(shift.getStartTime()) : null;
                LocalTime endTime = shift != null ? parseTime(shift.getEndTime()) : null;

                // Tìm server record trùng khớp
                AttendanceRecordItemResponse matchedRecord = null;
                for (AttendanceRecordItemResponse rec : serverRecords) {
                    if (matchedServerRecords.contains(rec)) continue;
                    LocalDate recDate = parseDate(rec.getWorkday());
                    if (scheduleDate.equals(recDate)) {
                        boolean matchStaff = (schedule.getEmployeeId() != null && schedule.getEmployeeId().equals(rec.getStaffId()))
                                || (schedule.getName() != null && schedule.getName().equalsIgnoreCase(rec.getStaffName()));
                        boolean matchShift = shiftName.equalsIgnoreCase(rec.getShiftName());
                        if (matchStaff && matchShift) {
                            matchedRecord = rec;
                            break;
                        } else if (matchStaff && matchedRecord == null) {
                            matchedRecord = rec;
                        }
                    }
                }
                if (matchedRecord != null) {
                    matchedServerRecords.add(matchedRecord);
                }

                LocalTime checkIn = matchedRecord != null ? parseTime(matchedRecord.getCheckInTimeString()) : null;
                LocalTime checkOut = matchedRecord != null ? parseTime(matchedRecord.getCheckOutTimeString()) : null;

                // Check local prefs nếu hôm nay
                if (scheduleDate.equals(today) && sharedPreferenceApp != null && shift != null) {
                    if (checkIn == null) {
                        String localIn = sharedPreferenceApp.getShiftCheckInTime(shift.getShiftId(), today.toString(), shiftName);
                        if (localIn != null) checkIn = parseTime(localIn);
                    }
                    if (checkOut == null) {
                        String localOut = sharedPreferenceApp.getShiftCheckOutTime(shift.getShiftId(), today.toString(), shiftName);
                        if (localOut != null) checkOut = parseTime(localOut);
                    }
                }

                AttendanceStatus status;
                if (checkIn != null) {
                    if (startTime != null && checkIn.isAfter(startTime)) {
                        status = AttendanceStatus.LATE;
                    } else if (matchedRecord != null && "EARLY_LEAVE".equalsIgnoreCase(matchedRecord.getStatus())) {
                        status = AttendanceStatus.EARLY_LEAVE;
                    } else if (checkOut != null && endTime != null && checkOut.isBefore(endTime)) {
                        status = AttendanceStatus.EARLY_LEAVE;
                    } else {
                        status = AttendanceStatus.PRESENT;
                    }
                } else {
                    CheckState localState = (sharedPreferenceApp != null && shift != null) ?
                            sharedPreferenceApp.getShiftAttendanceState(shift.getShiftId(), today.toString(), shiftName)
                            : CheckState.NOT_CHECKED_IN;

                    if (localState == CheckState.CHECKED_IN || localState == CheckState.CHECKED_OUT) {
                        status = AttendanceStatus.PRESENT;
                    } else if (scheduleDate.isBefore(today)) {
                        status = AttendanceStatus.ABSENT;
                    } else if (scheduleDate.equals(today)) {
                        if (endTime != null && now.isAfter(endTime)) {
                            status = AttendanceStatus.ABSENT;
                        } else if (startTime != null && now.isAfter(startTime)) {
                            status = AttendanceStatus.LATE;
                        } else {
                            status = AttendanceStatus.NOT_CHECKED;
                        }
                    } else {
                        status = AttendanceStatus.NOT_CHECKED;
                    }
                }

                String avatar = schedule.getAvatar();
                if (avatar == null && schedule.getEmployeeId() != null && staffMap.containsKey(schedule.getEmployeeId())) {
                    avatar = staffMap.get(schedule.getEmployeeId()).getAvatar();
                }

                result.add(new ManagerAttendanceHistoryItem(
                        UUID.randomUUID().toString(),
                        schedule.getEmployeeId(),
                        schedule.getName(),
                        avatar,
                        scheduleDate,
                        shiftName,
                        checkIn,
                        checkOut,
                        status,
                        startTime,
                        endTime,
                        ""
                ));
            }
        }

        // 2. Thêm các server record chưa nằm trong lịch
        for (AttendanceRecordItemResponse rec : serverRecords) {
            if (matchedServerRecords.contains(rec)) continue;
            LocalDate recDate = parseDate(rec.getWorkday());
            if (recDate == null || recDate.isBefore(startDate) || recDate.isAfter(endDate)) continue;

            LocalTime checkIn = parseTime(rec.getCheckInTimeString());
            LocalTime checkOut = parseTime(rec.getCheckOutTimeString());
            LocalTime startTime = parseTime(rec.getStartTime());
            LocalTime endTime = parseTime(rec.getEndTime());

            AttendanceStatus status = mapStatus(rec.getStatus());
            if (checkIn != null && startTime != null && checkIn.isAfter(startTime)) {
                status = AttendanceStatus.LATE;
            } else if (checkIn == null) {
                if (recDate.isBefore(today) || (recDate.equals(today) && endTime != null && now.isAfter(endTime))) {
                    status = AttendanceStatus.ABSENT;
                }
            }

            String avatar = rec.getAvatar();
            if (avatar == null && rec.getStaffId() != null && staffMap.containsKey(rec.getStaffId())) {
                avatar = staffMap.get(rec.getStaffId()).getAvatar();
            }

            result.add(new ManagerAttendanceHistoryItem(
                    UUID.randomUUID().toString(),
                    rec.getStaffId(),
                    rec.getStaffName() != null ? rec.getStaffName() : "Nhân viên",
                    avatar,
                    recDate,
                    rec.getShiftName() != null ? rec.getShiftName() : "Ca làm việc",
                    checkIn,
                    checkOut,
                    status,
                    startTime,
                    endTime,
                    ""
            ));
        }

        // Sắp xếp: Ngày mới nhất -> Giờ bắt đầu mới nhất -> Tên nhân viên A-Z
        result.sort(Comparator.comparing(
                ManagerAttendanceHistoryItem::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())
        ).thenComparing(
                item -> item.getStartTime() != null ? item.getStartTime() : (item.getCheckIn() != null ? item.getCheckIn() : LocalTime.MIDNIGHT),
                Comparator.nullsLast(Comparator.reverseOrder())
        ).thenComparing(ManagerAttendanceHistoryItem::getStaffName));

        return result;
    }

    private List<ManagerAttendanceHistoryItem> generateSampleData(LocalDate startDate, LocalDate endDate) {
        List<ManagerAttendanceHistoryItem> samples = new ArrayList<>();
        LocalDate today = LocalDate.now();

        String[] sampleNames = {"Nguyễn Văn An", "Trần Thị Mai", "Lê Hoàng Long", "Phạm Minh Tuấn", "Đỗ Thu Hà"};
        String[] sampleAvatars = {null, null, null, null, null};

        LocalDate cursor = endDate.isAfter(today) ? today : endDate;
        while (!cursor.isBefore(startDate)) {
            // Mỗi ngày tạo dữ liệu chấm công cho các nhân viên
            for (int i = 0; i < sampleNames.length; i++) {
                String name = sampleNames[i];
                String avatar = sampleAvatars[i];
                String shiftName = (i % 2 == 0) ? "Ca Sáng" : "Ca Chiều";
                LocalTime start = (i % 2 == 0) ? LocalTime.of(8, 0) : LocalTime.of(13, 30);
                LocalTime end = (i % 2 == 0) ? LocalTime.of(17, 0) : LocalTime.of(21, 30);

                LocalTime in = null;
                LocalTime out = null;
                AttendanceStatus status;

                if (i == 0) {
                    in = start.minusMinutes(10);
                    out = end;
                    status = AttendanceStatus.PRESENT;
                } else if (i == 1) {
                    in = start.plusMinutes(20);
                    out = end.plusMinutes(15);
                    status = AttendanceStatus.LATE;
                } else if (i == 2) {
                    in = start;
                    out = end.minusMinutes(45);
                    status = AttendanceStatus.EARLY_LEAVE;
                } else if (i == 3) {
                    if (cursor.equals(today)) {
                        status = AttendanceStatus.PRESENT;
                        in = start.minusMinutes(5);
                    } else {
                        status = AttendanceStatus.ABSENT;
                    }
                } else {
                    in = start.minusMinutes(2);
                    out = end;
                    status = AttendanceStatus.PRESENT;
                }

                samples.add(new ManagerAttendanceHistoryItem(
                        UUID.randomUUID().toString(),
                        "staff_" + i,
                        name,
                        avatar,
                        cursor,
                        shiftName,
                        in,
                        out,
                        status,
                        start,
                        end,
                        ""
                ));
            }

            cursor = cursor.minusDays(1);
        }

        return samples;
    }

    private void applyFilterAndUpdate() {
        List<ManagerAttendanceHistoryItem> filtered;
        if (currentFilter == null) {
            filtered = new ArrayList<>(allItems);
        } else {
            filtered = allItems.stream()
                    .filter(item -> item.getStatus() == currentFilter)
                    .collect(Collectors.toList());
        }
        _attendanceList.setValue(filtered);
    }

    private void updateStats() {
        int present = 0;
        int late = 0;
        int absent = 0;
        int earlyLeave = 0;

        for (ManagerAttendanceHistoryItem item : allItems) {
            if (item.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (item.getStatus() == AttendanceStatus.LATE) late++;
            else if (item.getStatus() == AttendanceStatus.ABSENT) absent++;
            else if (item.getStatus() == AttendanceStatus.EARLY_LEAVE) earlyLeave++;
        }

        _summaryStats.setValue(new SummaryStats(present, late, absent, earlyLeave));
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                } catch (Exception ignored) {}
            }
            return null;
        }
    }

    private LocalTime parseTime(Object timeObj) {
        if (timeObj == null) return null;
        String timeStr = timeObj.toString().trim();
        if (timeStr.isEmpty()) return null;
        try {
            if (timeStr.matches("^\\d+(\\.\\d+)?$")) {
                double seconds = Double.parseDouble(timeStr);
                return java.time.Instant.ofEpochMilli((long) (seconds * 1000))
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalTime();
            }
            if (timeStr.contains("T")) {
                return LocalDateTime.parse(timeStr).toLocalTime();
            }
            if (timeStr.length() >= 5) {
                return LocalTime.parse(timeStr.substring(0, 5));
            }
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private AttendanceStatus mapStatus(String statusStr) {
        if (statusStr == null) return AttendanceStatus.NOT_CHECKED;
        switch (statusStr.toUpperCase(Locale.US)) {
            case "OK":
            case "PRESENT":
            case "ON_TIME":
                return AttendanceStatus.PRESENT;
            case "NOT CHECKED OUT":
                return AttendanceStatus.PRESENT;
            case "LATE":
                return AttendanceStatus.LATE;
            case "EARLY_LEAVE":
                return AttendanceStatus.EARLY_LEAVE;
            case "ABSENT":
            case "NOT CHECKED IN":
            case "NOT ATTENDED":
                return AttendanceStatus.ABSENT;
            case "LEAVE":
            case "HOLIDAY":
                return AttendanceStatus.LEAVE;
            default:
                return AttendanceStatus.NOT_CHECKED;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
