package com.example.attendanceapp.ui.employee.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.IntervalTicker;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordItemResponse;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordPageResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentEmployeeHistoryBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.HistoryAttendance;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.domain.repository.AttendanceRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.usecase.schedule.GetScheduleByDateUseCase;
import com.example.attendanceapp.ui.employee.history.calendar.EmployeeCalendarDayAdapter;
import io.reactivex.rxjava3.core.Single;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeHistoryFragment extends Fragment {

    private FragmentEmployeeHistoryBinding binding;
    private EmployeeHistoryAttendanceAdapter employeeHistoryAttendanceAdapter;
    private EmployeeCalendarDayAdapter employeeCalendarDayAdapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    AttendanceRepository attendanceRepository;

    @Inject
    SessionRepository sessionRepository;

    @Inject
    GetScheduleByDateUseCase getScheduleByDateUseCase;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private IntervalTicker liveTicker;

    private YearMonth currentMonth;
    @Nullable
    private AttendanceStatus currentFilter = null; // null đại diện cho Filter ALL (Tất cả)

    private final List<HistoryAttendance> allAttendances = new ArrayList<>();

    public EmployeeHistoryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentMonth = YearMonth.now();
        liveTicker = new IntervalTicker(10000, this::checkAndUpdateLiveStatus);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (liveTicker != null) {
            liveTicker.start();
        }
        loadDataForCurrentMonth();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (liveTicker != null) {
            liveTicker.stop();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.includeHeader.headerSection, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupViews();
        setupListeners();
        setupObservers();

        updateMonthTitle();
        loadDataForCurrentMonth();
    }

    private void setupViews() {
        // 1. Setup RecyclerView Danh sách
        employeeHistoryAttendanceAdapter = new EmployeeHistoryAttendanceAdapter(new ArrayList<>());
        binding.includeLayoutRv.rvHistory.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false));
        binding.includeLayoutRv.rvHistory.setAdapter(employeeHistoryAttendanceAdapter);

        binding.includeLayoutRv.rvHistory.setHasFixedSize(false);

        // Mặc định chọn chip Tất cả
        binding.includeLayoutRv.chipGroupFilter.check(R.id.chip_all);
        currentFilter = null;

        // 2. Setup RecyclerView Lịch (GridLayout 7 cột)
        employeeCalendarDayAdapter = new EmployeeCalendarDayAdapter(new ArrayList<>());
        employeeCalendarDayAdapter.setOnDayClickListener(this::onCalendarDayClicked);
        binding.includeCalendar.rvCalendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.includeCalendar.rvCalendarGrid.setAdapter(employeeCalendarDayAdapter);
        binding.includeCalendar.rvCalendarGrid.setHasFixedSize(true);
    }

    private void onCalendarDayClicked(WeekDay day) {
        Toast.makeText(requireContext(),
                "Ngày: " + day.getDate() + " - " + day.getStatus().getDisplayName(),
                Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        // Toggle chế độ Danh sách / Lịch
        binding.includeHeader.toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_view_list) {
                    binding.includeLayoutRv.getRoot().setVisibility(View.VISIBLE);
                    binding.includeCalendar.getRoot().setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_view_calendar) {
                    binding.includeLayoutRv.getRoot().setVisibility(View.GONE);
                    binding.includeCalendar.getRoot().setVisibility(View.VISIBLE);
                }
            }
        });

        binding.includeHeader.btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthTitle();
            loadDataForCurrentMonth();
        });

        binding.includeHeader.btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthTitle();
            loadDataForCurrentMonth();
        });

        // Lọc theo ChipGroup
        binding.includeLayoutRv.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                group.check(R.id.chip_all);
                currentFilter = null;
                applyFilterAndUpdateList();
                return;
            }
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_present) {
                currentFilter = AttendanceStatus.PRESENT;
            } else if (checkedId == R.id.chip_late) {
                currentFilter = AttendanceStatus.LATE;
            } else if (checkedId == R.id.chip_absent) {
                currentFilter = AttendanceStatus.ABSENT;
            } else if (checkedId == R.id.chip_early) {
                currentFilter = AttendanceStatus.EARLY_LEAVE;
            } else {
                currentFilter = null; // Tất cả
            }

            applyFilterAndUpdateList();
        });
    }

    private void loadDataForCurrentMonth() {
        String startDate = currentMonth.atDay(1).toString();
        String endDate = currentMonth.atEndOfMonth().toString();

        if (sessionRepository == null) return;

        Single<AttendanceRecordPageResponse> attendanceSingle = sessionRepository.getCurrentUser()
                .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                .defaultIfEmpty("")
                .flatMap(managerId -> {
                    if (managerId.isEmpty() || attendanceRepository == null) {
                        return Single.just(new AttendanceRecordPageResponse());
                    }
                    return attendanceRepository.getAttendanceRecords(managerId, startDate, endDate, 1, 100)
                            .onErrorReturnItem(new AttendanceRecordPageResponse());
                })
                .onErrorReturnItem(new AttendanceRecordPageResponse());

        Single<List<StaffWorkScheduleResponse>> scheduleSingle;
        if (getScheduleByDateUseCase != null) {
            scheduleSingle = getScheduleByDateUseCase.execute(null)
                    .onErrorReturnItem(new ArrayList<>());
        } else {
            scheduleSingle = Single.just(new ArrayList<>());
        }

        disposables.add(Single.zip(attendanceSingle, scheduleSingle, (attPage, schedules) ->
                        buildHistoryList(attPage, schedules, currentMonth))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        attendances -> {
                            allAttendances.clear();
                            if (attendances != null && !attendances.isEmpty()) {
                                allAttendances.addAll(attendances);
                            } else {
                                allAttendances.addAll(getSampleAttendances());
                            }
                            applyFilterAndUpdateList();
                            updateSummaryStats();
                            updateCalendar();
                        },
                        error -> {
                            allAttendances.clear();
                            allAttendances.addAll(getSampleAttendances());
                            applyFilterAndUpdateList();
                            updateSummaryStats();
                            updateCalendar();
                        }
                )
        );
    }

    private List<HistoryAttendance> buildHistoryList(
            AttendanceRecordPageResponse attPage,
            List<StaffWorkScheduleResponse> schedules,
            YearMonth targetMonth
    ) {
        List<HistoryAttendance> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<AttendanceRecordItemResponse> serverRecords = (attPage != null && attPage.getData() != null)
                ? attPage.getData()
                : Collections.emptyList();

        Set<AttendanceRecordItemResponse> matchedServerRecords = new HashSet<>();

        // 1. Duyệt qua ca làm việc trong lịch (StaffWorkScheduleResponse) của tháng targetMonth
        if (schedules != null) {
            for (StaffWorkScheduleResponse schedule : schedules) {
                if (schedule == null || schedule.getWorkDay() == null) continue;
                LocalDate scheduleDate = parseDate(schedule.getWorkDay());
                if (scheduleDate == null) continue;
                if (!YearMonth.from(scheduleDate).equals(targetMonth)) continue;

                if (schedule.getShifts() == null) continue;
                for (StaffWorkScheduleResponse.ShiftInfo shift : schedule.getShifts()) {
                    if (shift == null) continue;

                    String shiftName = shift.getShiftName() != null ? shift.getShiftName() : "";
                    LocalTime startTime = parseTime(shift.getStartTime());
                    LocalTime endTime = parseTime(shift.getEndTime());

                    // Tìm bản ghi server khớp ngày và ca làm việc
                    AttendanceRecordItemResponse matchedRecord = null;
                    for (AttendanceRecordItemResponse rec : serverRecords) {
                        if (matchedServerRecords.contains(rec)) continue;
                        LocalDate recDate = parseDate(rec.getWorkday());
                        if (scheduleDate.equals(recDate)) {
                            if (shiftName.equalsIgnoreCase(rec.getShiftName())) {
                                matchedRecord = rec;
                                break;
                            } else if (matchedRecord == null) {
                                matchedRecord = rec;
                            }
                        }
                    }
                    if (matchedRecord != null) {
                        matchedServerRecords.add(matchedRecord);
                    }

                    // Thời gian check-in
                    LocalTime checkIn = matchedRecord != null ? parseTime(matchedRecord.getCheckInTime()) : null;
                    if (checkIn == null && sharedPreferenceApp != null) {
                        String localIn = sharedPreferenceApp.getShiftCheckInTime(
                                shift.getWorkScheduleId(), schedule.getWorkDay(), shiftName
                        );
                        if (localIn != null) checkIn = parseTime(localIn);
                    }

                    // Thời gian check-out
                    LocalTime checkOut = matchedRecord != null ? parseTime(matchedRecord.getCheckOutTime()) : null;
                    if (checkOut == null && sharedPreferenceApp != null) {
                        String localOut = sharedPreferenceApp.getShiftCheckOutTime(
                                shift.getWorkScheduleId(), schedule.getWorkDay(), shiftName
                        );
                        if (localOut != null) checkOut = parseTime(localOut);
                    }

                    // Xác định trạng thái điểm danh
                    AttendanceStatus status;
                    if (checkIn != null) {
                        if (startTime != null && checkIn.isAfter(startTime)) {
                            status = AttendanceStatus.LATE; // Đi muộn
                        } else if (matchedRecord != null && "EARLY_LEAVE".equalsIgnoreCase(matchedRecord.getStatus())) {
                            status = AttendanceStatus.EARLY_LEAVE;
                        } else if (checkOut != null && endTime != null && checkOut.isBefore(endTime)) {
                            status = AttendanceStatus.EARLY_LEAVE;
                        } else {
                            status = AttendanceStatus.PRESENT; // Đúng giờ
                        }
                    } else {
                        CheckState localState = sharedPreferenceApp != null ? sharedPreferenceApp.getShiftAttendanceState(
                                shift.getWorkScheduleId(), schedule.getWorkDay(), shiftName
                        ) : CheckState.NOT_CHECKED_IN;

                        if (localState == CheckState.CHECKED_IN || localState == CheckState.CHECKED_OUT) {
                            status = AttendanceStatus.PRESENT;
                        } else if (scheduleDate.isBefore(today)) {
                            status = AttendanceStatus.ABSENT; // Ngày quá khứ không chấm công -> Vắng mặt
                        } else if (scheduleDate.equals(today)) {
                            if (endTime != null && now.isAfter(endTime)) {
                                // Đúng giờ vắng mặt: Hết ca mà chưa chấm công -> Vắng mặt
                                status = AttendanceStatus.ABSENT;
                            } else if (startTime != null && now.isAfter(startTime)) {
                                // Quá giờ bắt đầu ca mà chưa check-in -> Đi muộn
                                status = AttendanceStatus.LATE;
                            } else {
                                status = AttendanceStatus.NOT_CHECKED;
                            }
                        } else {
                            status = AttendanceStatus.NOT_CHECKED;
                        }
                    }

                    String location = resolveShiftLocation(shiftName);

                    list.add(new HistoryAttendance(
                            UUID.randomUUID().toString(),
                            "",
                            scheduleDate,
                            checkIn,
                            checkOut,
                            status,
                            location,
                            startTime,
                            endTime,
                            shiftName
                    ));
                }
            }
        }

        // 2. Bổ sung các server record chưa nằm trong lịch
        for (AttendanceRecordItemResponse rec : serverRecords) {
            if (matchedServerRecords.contains(rec)) continue;
            LocalDate recDate = parseDate(rec.getWorkday());
            if (recDate == null || !YearMonth.from(recDate).equals(targetMonth)) continue;

            LocalTime checkIn = parseTime(rec.getCheckInTime());
            LocalTime checkOut = parseTime(rec.getCheckOutTime());
            LocalTime startTime = parseTime(rec.getStartTime());
            LocalTime endTime = parseTime(rec.getEndTime());

            AttendanceStatus status = mapStatus(rec.getStatus());
            if (checkIn != null && startTime != null && checkIn.isAfter(startTime)) {
                status = AttendanceStatus.LATE; // Đi muộn
            } else if (checkIn == null) {
                if (recDate.isBefore(today)) {
                    status = AttendanceStatus.ABSENT;
                } else if (recDate.equals(today) && endTime != null && now.isAfter(endTime)) {
                    status = AttendanceStatus.ABSENT;
                } else if (recDate.equals(today) && startTime != null && now.isAfter(startTime)) {
                    status = AttendanceStatus.LATE;
                }
            }

            String shiftName = rec.getShiftName() != null ? rec.getShiftName() : "";
            String location = resolveShiftLocation(shiftName);

            list.add(new HistoryAttendance(
                    UUID.randomUUID().toString(),
                    "",
                    recDate,
                    checkIn,
                    checkOut,
                    status,
                    location,
                    startTime,
                    endTime,
                    shiftName
            ));
        }

        // 3. Sắp xếp danh sách: Mới nhất lên đầu
        list.sort(Comparator.comparing(
                HistoryAttendance::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())
        ).thenComparing(
                item -> item.getStartTime() != null ? item.getStartTime() : (item.getCheckIn() != null ? item.getCheckIn() : LocalTime.MIDNIGHT),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        if (list.isEmpty()) {
            return getSampleAttendances();
        }

        return list;
    }

    private void checkAndUpdateLiveStatus() {
        if (binding == null || allAttendances.isEmpty()) return;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        boolean hasChanged = false;

        for (HistoryAttendance item : allAttendances) {
            if (item == null || item.getDate() == null || !item.getDate().equals(today)) continue;
            if (item.getCheckIn() != null) continue; // Đã check-in thì không đổi trạng thái theo thời gian

            LocalTime startTime = item.getStartTime();
            LocalTime endTime = item.getEndTime();
            AttendanceStatus oldStatus = item.getStatus();
            AttendanceStatus newStatus = oldStatus;

            if (endTime != null && now.isAfter(endTime)) {
                // Đúng giờ vắng mặt: Tự động cập nhật thành Vắng mặt
                newStatus = AttendanceStatus.ABSENT;
            } else if (startTime != null && now.isAfter(startTime)) {
                // Quá giờ bắt đầu ca: Tự động cập nhật thành Đi muộn
                newStatus = AttendanceStatus.LATE;
            }

            if (oldStatus != newStatus) {
                item.setStatus(newStatus);
                hasChanged = true;
            }
        }

        if (hasChanged) {
            applyFilterAndUpdateList();
            updateSummaryStats();
            updateCalendar();
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                try {
                    return LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
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

    private void updateCalendar() {
        List<WeekDay> monthDays = createMonthDays(currentMonth);
        employeeCalendarDayAdapter.submitList(monthDays);
    }

    private List<WeekDay> createMonthDays(YearMonth yearMonth) {
        List<WeekDay> days = new ArrayList<>();
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();
        LocalDate today = LocalDate.now();

        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int emptyDaysBefore = dayOfWeek - 1;

        for (int i = 0; i < emptyDaysBefore; i++) {
            days.add(new WeekDay(
                    firstOfMonth.minusDays(emptyDaysBefore - i),
                    AttendanceStatus.WEEKEND,
                    false
            ));
        }

        for (int day = 1; day <= lastOfMonth.getDayOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            AttendanceStatus status = getAttendanceStatusForDate(date);
            boolean isToday = date.equals(today);

            days.add(new WeekDay(date, status, isToday));
        }

        return days;
    }

    private AttendanceStatus getAttendanceStatusForDate(LocalDate date) {
        for (HistoryAttendance attendance : allAttendances) {
            if (attendance.getDate().equals(date)) {
                return attendance.getStatus();
            }
        }
        return AttendanceStatus.NOT_CHECKED;
    }

    private String resolveShiftLocation(String shiftName) {
        if (sharedPreferenceApp != null) {
            String addr = sharedPreferenceApp.getShiftAddress(shiftName);
            if (addr != null && !addr.trim().isEmpty()) {
                return addr.trim();
            }
            String latestAddr = sharedPreferenceApp.getLatestShiftAddress();
            if (latestAddr != null && !latestAddr.trim().isEmpty()) {
                return latestAddr.trim();
            }
            String compAddr = sharedPreferenceApp.getCompanyAddress();
            if (compAddr != null && !compAddr.trim().isEmpty()) {
                return compAddr.trim();
            }
        }
        return "Vị trí ca làm việc thực tế";
    }

    private List<HistoryAttendance> getSampleAttendances() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Kiểm tra xem hôm nay có check-in / check-out trong SharedPreferences không
        LocalTime todayCheckIn = null;
        LocalTime todayCheckOut = null;
        AttendanceStatus todayStatus = AttendanceStatus.NOT_CHECKED;

        if (sharedPreferenceApp != null) {
            String inTime = sharedPreferenceApp.getShiftCheckInTime(null, today.toString(), null);
            if (inTime != null) {
                todayCheckIn = parseTime(inTime);
            }
            String outTime = sharedPreferenceApp.getShiftCheckOutTime(null, today.toString(), null);
            if (outTime != null) {
                todayCheckOut = parseTime(outTime);
            }
        }

        LocalTime shiftStart = LocalTime.of(8, 0);
        LocalTime shiftEnd = LocalTime.of(17, 0);

        if (todayCheckIn != null) {
            todayStatus = todayCheckIn.isAfter(shiftStart) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        } else {
            if (now.isAfter(shiftEnd)) {
                todayStatus = AttendanceStatus.ABSENT; // Đúng giờ vắng mặt
            } else if (now.isAfter(shiftStart)) {
                todayStatus = AttendanceStatus.LATE; // Đi muộn
            } else {
                todayStatus = AttendanceStatus.NOT_CHECKED;
            }
        }

        List<HistoryAttendance> samples = new ArrayList<>();
        // 1. Ca hôm nay
        samples.add(new HistoryAttendance(
                UUID.randomUUID().toString(),
                "",
                today,
                todayCheckIn,
                todayCheckOut,
                todayStatus,
                resolveShiftLocation("Ca Sáng"),
                shiftStart,
                shiftEnd,
                "Ca Sáng"
        ));

        // 2. Các ngày trước đó trong tháng (mới nhất lên đầu)
        if (today.getDayOfMonth() > 1) {
            LocalDate d1 = today.minusDays(1);
            samples.add(new HistoryAttendance(
                    UUID.randomUUID().toString(),
                    "",
                    d1,
                    LocalTime.of(8, 15),
                    LocalTime.of(17, 30),
                    AttendanceStatus.LATE,
                    resolveShiftLocation("Ca Sáng"),
                    shiftStart,
                    shiftEnd,
                    "Ca Sáng"
            ));
        }
        if (today.getDayOfMonth() > 2) {
            LocalDate d2 = today.minusDays(2);
            samples.add(new HistoryAttendance(
                    UUID.randomUUID().toString(),
                    "",
                    d2,
                    LocalTime.of(7, 55),
                    LocalTime.of(17, 0),
                    AttendanceStatus.PRESENT,
                    resolveShiftLocation("Ca Sáng"),
                    shiftStart,
                    shiftEnd,
                    "Ca Sáng"
            ));
        }
        if (today.getDayOfMonth() > 3) {
            LocalDate d3 = today.minusDays(3);
            samples.add(new HistoryAttendance(
                    UUID.randomUUID().toString(),
                    "",
                    d3,
                    null,
                    null,
                    AttendanceStatus.ABSENT,
                    resolveShiftLocation("Ca Sáng"),
                    shiftStart,
                    shiftEnd,
                    "Ca Sáng"
            ));
        }
        if (today.getDayOfMonth() > 4) {
            LocalDate d4 = today.minusDays(4);
            samples.add(new HistoryAttendance(
                    UUID.randomUUID().toString(),
                    "",
                    d4,
                    LocalTime.of(8, 0),
                    LocalTime.of(16, 30),
                    AttendanceStatus.EARLY_LEAVE,
                    resolveShiftLocation("Ca Sáng"),
                    shiftStart,
                    shiftEnd,
                    "Ca Sáng"
            ));
        }

        return samples;
    }

    private void applyFilterAndUpdateList() {
        List<HistoryAttendance> filtered;

        if (currentFilter == null) {
            filtered = new ArrayList<>(allAttendances);
        } else {
            filtered = allAttendances.stream()
                    .filter(item -> item.getStatus() == currentFilter)
                    .collect(Collectors.toList());
        }

        employeeHistoryAttendanceAdapter.updateData(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.includeLayoutRv.rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includeLayoutRv.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateSummaryStats() {
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int earlyLeaveCount = 0;
        long totalWorkingMinutes = 0;

        for (HistoryAttendance item : allAttendances) {
            if (item.getStatus() == AttendanceStatus.PRESENT) {
                presentCount++;
            } else if (item.getStatus() == AttendanceStatus.LATE) {
                lateCount++;
            } else if (item.getStatus() == AttendanceStatus.ABSENT) {
                absentCount++;
            } else if (item.getStatus() == AttendanceStatus.EARLY_LEAVE) {
                earlyLeaveCount++;
            }

            if (item.getCheckIn() != null && item.getCheckOut() != null) {
                Duration duration = Duration.between(item.getCheckIn(), item.getCheckOut());
                if (!duration.isNegative()) {
                    totalWorkingMinutes += duration.toMinutes();
                }
            }
        }

        binding.includeHeader.tvOnTimeCount.setText(String.valueOf(presentCount));
        binding.includeHeader.tvLateCount.setText(String.valueOf(lateCount));
        binding.includeHeader.tvAbsCount.setText(String.valueOf(absentCount));
        binding.includeHeader.tvEarlyLeaveCount.setText(String.valueOf(earlyLeaveCount));

        double totalHours = totalWorkingMinutes / 60.0;
        binding.includeHeader.tvTotalWorkingHours.setText(String.format(Locale.US, "%.1f giờ", totalHours));
    }

    private void updateMonthTitle() {
        String monthName = currentMonth
                .getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));
        int year = currentMonth.getYear();
        String formattedTitle = capitalizeFirstLetter(monthName) + " " + year;

        binding.includeHeader.tvMonthYearSubtitle.setText(formattedTitle);
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + text.substring(1);
    }

    private void setupObservers() {
    }

    @Override
    public void onDestroyView() {
        if (liveTicker != null) {
            liveTicker.stop();
            liveTicker = null;
        }
        disposables.clear();
        binding = null;
        super.onDestroyView();
    }
}
