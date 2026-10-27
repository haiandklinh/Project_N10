package com.example.attendanceapp.domain.usecase.home;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.repository.ScheduleRepository;
import com.example.attendanceapp.domain.repository.ShiftRepository;
import com.example.attendanceapp.ui.manager.home.TodayShiftSummary;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

/**
 * Lấy thông tin ca làm việc ngày hôm nay cho màn hình Manager Home.
 */
public class GetTodayShiftUseCase {

    private final ScheduleRepository scheduleRepository;
    private final ShiftRepository shiftRepository;
    private final SharedPreferenceApp sharedPreferenceApp;

    @Inject
    public GetTodayShiftUseCase(ScheduleRepository scheduleRepository,
                                ShiftRepository shiftRepository,
                                SharedPreferenceApp sharedPreferenceApp) {
        this.scheduleRepository = scheduleRepository;
        this.shiftRepository = shiftRepository;
        this.sharedPreferenceApp = sharedPreferenceApp;
    }

    /**
     * Quan sát stream ca làm việc hôm nay từ Room DB.
     */
    public Observable<List<TodayShiftSummary>> execute() {
        return Observable.combineLatest(
                scheduleRepository.observeEmployeeWorkSchedules(),
                shiftRepository.observeShifts(),
                (schedules, shiftOptions) -> mapToTodayShifts(schedules, shiftOptions, LocalDate.now())
        );
    }

    /**
     * Làm mới lịch làm việc và danh sách ca từ server ở nền.
     */
    public Completable refresh() {
        return Completable.mergeArrayDelayError(
                scheduleRepository.getEmployeeWorkSchedules().ignoreElement().onErrorComplete(),
                shiftRepository.getShifts().ignoreElement().onErrorComplete()
        );
    }

    public List<TodayShiftSummary> mapToTodayShifts(
            List<EmployeeWorkSchedule> schedules,
            List<ShiftOption> shiftOptions,
            LocalDate targetDate
    ) {
        if (targetDate == null) {
            targetDate = LocalDate.now();
        }

        // 1. Tạo bản đồ tra cứu ShiftOption theo ID và Tiêu đề
        Map<String, ShiftOption> shiftOptionById = new HashMap<>();
        Map<String, ShiftOption> shiftOptionByTitle = new HashMap<>();
        if (shiftOptions != null) {
            for (ShiftOption opt : shiftOptions) {
                if (opt != null) {
                    if (opt.getId() != null && !opt.getId().trim().isEmpty()) {
                        shiftOptionById.put(opt.getId().trim(), opt);
                    }
                    if (opt.getTitle() != null && !opt.getTitle().trim().isEmpty()) {
                        shiftOptionByTitle.put(opt.getTitle().trim().toLowerCase(Locale.ROOT), opt);
                    }
                }
            }
        }

        // 2. Lọc các bản ghi lịch làm việc của targetDate có ca làm việc
        List<EmployeeWorkSchedule> todaySchedules = new ArrayList<>();
        if (schedules != null) {
            for (EmployeeWorkSchedule s : schedules) {
                if (s != null && s.getShift() != null && isDateMatch(s.getWorkDate(), targetDate)) {
                    todaySchedules.add(s);
                }
            }
        }

        if (todaySchedules.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Gom nhóm theo ca làm việc (theo shiftId hoặc shiftName)
        Map<String, List<EmployeeWorkSchedule>> schedulesByShift = new LinkedHashMap<>();
        for (EmployeeWorkSchedule s : todaySchedules) {
            EmployeeWorkSchedule.ShiftInfo shift = s.getShift();
            String key = null;
            if (shift.getShiftId() != null && !shift.getShiftId().trim().isEmpty()) {
                key = shift.getShiftId().trim();
            } else if (shift.getShiftName() != null && !shift.getShiftName().trim().isEmpty()) {
                key = shift.getShiftName().trim().toLowerCase(Locale.ROOT);
            }

            if (key != null) {
                if (!schedulesByShift.containsKey(key)) {
                    schedulesByShift.put(key, new ArrayList<>());
                }
                schedulesByShift.get(key).add(s);
            }
        }

        // 4. Xây dựng danh sách TodayShiftSummary
        List<TodayShiftSummary> result = new ArrayList<>();
        for (Map.Entry<String, List<EmployeeWorkSchedule>> entry : schedulesByShift.entrySet()) {
            List<EmployeeWorkSchedule> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;

            EmployeeWorkSchedule first = list.get(0);
            EmployeeWorkSchedule.ShiftInfo shiftInfo = first.getShift();

            String shiftId = shiftInfo.getShiftId();
            String shiftName = shiftInfo.getShiftName();
            String startTime = shiftInfo.getStartTime();
            String endTime = shiftInfo.getEndTime();

            // Đối chiếu với ShiftOption cấu hình trong hệ thống
            ShiftOption opt = null;
            if (shiftId != null && shiftOptionById.containsKey(shiftId.trim())) {
                opt = shiftOptionById.get(shiftId.trim());
            } else if (shiftName != null && shiftOptionByTitle.containsKey(shiftName.trim().toLowerCase(Locale.ROOT))) {
                opt = shiftOptionByTitle.get(shiftName.trim().toLowerCase(Locale.ROOT));
            }

            if (opt != null) {
                if (shiftName == null || shiftName.trim().isEmpty()) {
                    shiftName = opt.getTitle();
                }
                if (startTime == null || startTime.trim().isEmpty()) {
                    startTime = opt.getStartTime();
                }
                if (endTime == null || endTime.trim().isEmpty()) {
                    endTime = opt.getEndTime();
                }
            }

            if (shiftName == null || shiftName.trim().isEmpty()) {
                shiftName = "Ca làm việc";
            }

            String timeRange = "";
            if (opt != null && opt.getTimeRange() != null && !opt.getTimeRange().trim().isEmpty()) {
                timeRange = opt.getTimeRange();
            } else if (startTime != null && endTime != null) {
                timeRange = formatTimeRange(startTime, endTime);
            }

            String location = getLocation(shiftName);

            // Đếm số lượng nhân viên khác nhau được xếp vào ca này
            Set<String> employeeIds = new HashSet<>();
            for (EmployeeWorkSchedule item : list) {
                if (item.getEmployeeId() != null && !item.getEmployeeId().trim().isEmpty()) {
                    employeeIds.add(item.getEmployeeId().trim());
                } else if (item.getName() != null && !item.getName().trim().isEmpty()) {
                    employeeIds.add(item.getName().trim());
                }
            }
            int employeeCount = Math.max(1, employeeIds.size());

            ShiftStatus status = calculateShiftStatus(startTime, endTime);

            result.add(new TodayShiftSummary(
                    shiftName,
                    timeRange,
                    location,
                    status.getDisplayName(),
                    employeeCount,
                    status
            ));
        }

        // 5. Sắp xếp theo giờ bắt đầu ca làm tăng dần
        result.sort((a, b) -> {
            LocalTime t1 = parseStartTime(a.getTimeRange());
            LocalTime t2 = parseStartTime(b.getTimeRange());
            if (t1 != null && t2 != null) {
                return t1.compareTo(t2);
            }
            if (t1 != null) return -1;
            if (t2 != null) return 1;
            return a.getShiftName().compareToIgnoreCase(b.getShiftName());
        });

        return result;
    }

    private String getLocation(String shiftName) {
        String loc = null;
        if (sharedPreferenceApp != null) {
            loc = sharedPreferenceApp.getShiftAddress(shiftName);
            if (loc == null || loc.trim().isEmpty()) {
                loc = sharedPreferenceApp.getLatestShiftAddress();
            }
            if (loc == null || loc.trim().isEmpty()) {
                loc = sharedPreferenceApp.getCompanyAddress();
            }
        }
        if (loc == null || loc.trim().isEmpty()) {
            loc = "Văn phòng chính";
        }
        return loc;
    }

    private static String formatTimeRange(String startTime, String endTime) {
        String s = formatTime(startTime);
        String e = formatTime(endTime);
        if (s.isEmpty() && e.isEmpty()) return "";
        if (s.isEmpty()) return e;
        if (e.isEmpty()) return s;
        return s + " - " + e;
    }

    private static String formatTime(String time) {
        if (time == null || time.trim().isEmpty()) return "";
        String t = time.trim();
        if (t.length() >= 5) {
            return t.substring(0, 5);
        }
        return t;
    }

    private static ShiftStatus calculateShiftStatus(String startTimeStr, String endTimeStr) {
        LocalTime start = parseTime(startTimeStr);
        LocalTime end = parseTime(endTimeStr);
        LocalTime now = LocalTime.now();

        if (start != null && end != null) {
            boolean isOvernight = end.isBefore(start);
            if (!isOvernight) {
                if (now.isBefore(start)) {
                    return ShiftStatus.PENDING;
                } else if (now.isAfter(end)) {
                    return ShiftStatus.COMPLETED;
                } else {
                    return ShiftStatus.WORKING;
                }
            } else {
                if (now.isAfter(start) || now.isBefore(end)) {
                    return ShiftStatus.WORKING;
                } else {
                    return ShiftStatus.PENDING;
                }
            }
        } else if (start != null) {
            if (now.isBefore(start)) {
                return ShiftStatus.PENDING;
            } else {
                return ShiftStatus.WORKING;
            }
        }
        return ShiftStatus.PENDING;
    }

    private static LocalTime parseTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalTime.parse(s.trim());
        } catch (Exception e) {
            try {
                if (s.trim().length() >= 5) {
                    return LocalTime.parse(s.trim().substring(0, 5));
                }
            } catch (Exception ignored) {}
            return null;
        }
    }

    private static LocalTime parseStartTime(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) return null;
        try {
            String[] parts = timeRange.split("-");
            if (parts.length > 0) {
                return parseTime(parts[0].trim());
            }
        } catch (Exception ignored) {}
        return null;
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
}
