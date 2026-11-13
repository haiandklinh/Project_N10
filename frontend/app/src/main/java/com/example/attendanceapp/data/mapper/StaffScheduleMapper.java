package com.example.attendanceapp.data.mapper;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.attendanceapp.core.utils.LocationUtils;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.model.WeekDay;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Chuyển lịch làm việc của nhân viên từ server (mỗi ngày kèm danh sách ca) sang model của UI.
 * Dùng chung cho màn Trang chủ và màn Lịch.
 */
public final class StaffScheduleMapper {

    private StaffScheduleMapper() { }

    /**
     * Tách mỗi (ngày, ca) thành một Shift, sắp theo ngày rồi giờ bắt đầu.
     */
    public static List<Shift> toShifts(List<StaffWorkScheduleResponse> schedules) {
        return toShifts(schedules, null, null, null, null);
    }

    public static List<Shift> toShifts(List<StaffWorkScheduleResponse> schedules, SharedPreferenceApp pref) {
        return toShifts(schedules, null, pref, null, null);
    }

    public static List<Shift> toShifts(
            @Nullable List<StaffWorkScheduleResponse> schedules,
            @Nullable List<ShiftOption> shiftOptions,
            @Nullable SharedPreferenceApp pref,
            @Nullable String employeeId,
            @Nullable String companyName
    ) {
        List<Shift> result = new ArrayList<>();
        if (schedules == null) return result;

        Map<String, ShiftOption> optionById = new HashMap<>();
        Map<String, ShiftOption> optionByTitle = new HashMap<>();
        indexShiftOptions(shiftOptions, optionById, optionByTitle);

        for (StaffWorkScheduleResponse item : schedules) {
            if (item == null || item.getShifts() == null) continue;

            LocalDate date = parseDate(item.getWorkDay());
            if (date == null) continue;

            for (StaffWorkScheduleResponse.ShiftInfo s : item.getShifts()) {
                if (s == null) continue;
                ShiftOption matchedOpt = findMatchingShiftOption(s, optionById, optionByTitle);
                if (matchedOpt != null && pref != null
                        && isValidCoords(matchedOpt.getLatitude(), matchedOpt.getLongitude())) {
                    double radius = matchedOpt.getCheckInRadius() != null && matchedOpt.getCheckInRadius() > 0
                            ? matchedOpt.getCheckInRadius() : 100.0;
                    if (s.getShiftName() != null && !s.getShiftName().trim().isEmpty()) {
                        pref.saveShiftLocation(s.getShiftName(), matchedOpt.getLatitude(), matchedOpt.getLongitude(), radius);
                    }
                }

                String location = resolveShiftLocation(s, matchedOpt, pref, companyName);

                LocalTime startTime = parseTime(s.getStartTime());
                LocalTime endTime = parseTime(s.getEndTime());
                LocalTime allowedCheckIn = parseTime(s.getAllowedCheckInTime());
                if (allowedCheckIn == null && matchedOpt != null) {
                    allowedCheckIn = parseTime(matchedOpt.getAllowedCheckInTime());
                }
                if (allowedCheckIn == null && pref != null) {
                    allowedCheckIn = parseTime(pref.getShiftAllowedCheckInTime(s.getShiftName()));
                }
                if (allowedCheckIn == null) {
                    allowedCheckIn = startTime;
                }

                CheckState checkState = CheckState.NOT_CHECKED_IN;
                if (pref != null) {
                    checkState = pref.getShiftAttendanceState(s.getWorkScheduleId(), item.getWorkDay(), s.getShiftName());
                }

                ShiftStatus status = calculateShiftStatus(date, startTime, endTime, checkState);

                result.add(new Shift(
                        s.getWorkScheduleId(),
                        employeeId,
                        date,
                        s.getShiftName(),
                        startTime,
                        endTime,
                        location,
                        status,
                        allowedCheckIn
                ));
            }
        }

        result.sort(Comparator.comparing(Shift::getDate)
                .thenComparing(Shift::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    /**
     * Reverse-geocode tọa độ của các ca làm chưa có địa chỉ văn bản vào SharedPreferenceApp.
     * Chỉ gọi trên luồng nền (Schedulers.io). Trả về true nếu có ít nhất 1 địa chỉ mới được cập nhật.
     */
    public static boolean resolveMissingShiftAddressesSync(
            @Nullable Context context,
            @Nullable List<StaffWorkScheduleResponse> schedules,
            @Nullable List<ShiftOption> shiftOptions,
            @Nullable SharedPreferenceApp pref
    ) {
        if (context == null || pref == null || schedules == null) return false;

        Map<String, ShiftOption> optionById = new HashMap<>();
        Map<String, ShiftOption> optionByTitle = new HashMap<>();
        indexShiftOptions(shiftOptions, optionById, optionByTitle);

        Map<String, StaffWorkScheduleResponse.ShiftInfo> uniqueShifts = new LinkedHashMap<>();
        for (StaffWorkScheduleResponse item : schedules) {
            if (item == null || item.getShifts() == null) continue;
            for (StaffWorkScheduleResponse.ShiftInfo s : item.getShifts()) {
                if (s == null || s.getShiftName() == null || s.getShiftName().trim().isEmpty()) continue;
                String key = s.getShiftName().trim().toLowerCase(Locale.ROOT);
                uniqueShifts.putIfAbsent(key, s);
            }
        }

        boolean updated = false;
        for (StaffWorkScheduleResponse.ShiftInfo s : uniqueShifts.values()) {
            String shiftName = s.getShiftName().trim();
            if (!isBlank(s.getAddress())) continue;
            if (!isBlank(pref.getExactShiftAddress(shiftName))) continue;

            ShiftOption matchedOpt = findMatchingShiftOption(s, optionById, optionByTitle);
            if (matchedOpt != null && !isBlank(matchedOpt.getTitle())
                    && !isBlank(pref.getExactShiftAddress(matchedOpt.getTitle()))) {
                continue;
            }

            double[] coords = findShiftCoordinates(s, matchedOpt, pref, true);
            if (coords == null) continue;

            String resolvedAddress = LocationUtils.resolveAddressFromLocationSync(context, coords[0], coords[1]);
            if (!isBlank(resolvedAddress)) {
                pref.saveShiftAddress(shiftName, resolvedAddress);
                if (matchedOpt != null && !isBlank(matchedOpt.getTitle())) {
                    pref.saveShiftAddress(matchedOpt.getTitle(), resolvedAddress);
                }
                if (isBlank(pref.getLatestShiftAddress())) {
                    double radius = matchedOpt != null && matchedOpt.getCheckInRadius() != null && matchedOpt.getCheckInRadius() > 0
                            ? matchedOpt.getCheckInRadius() : pref.getLatestShiftRadius();
                    pref.saveLatestShiftLocation(coords[0], coords[1], radius, resolvedAddress);
                }
                updated = true;
            }
        }
        return updated;
    }

    private static void indexShiftOptions(
            @Nullable List<ShiftOption> shiftOptions,
            Map<String, ShiftOption> optionById,
            Map<String, ShiftOption> optionByTitle
    ) {
        if (shiftOptions == null) return;
        for (ShiftOption opt : shiftOptions) {
            if (opt == null) continue;
            if (!isBlank(opt.getId())) {
                optionById.put(opt.getId().trim(), opt);
            }
            if (!isBlank(opt.getTitle())) {
                optionByTitle.put(opt.getTitle().trim().toLowerCase(Locale.ROOT), opt);
            }
        }
    }

    @Nullable
    private static ShiftOption findMatchingShiftOption(
            StaffWorkScheduleResponse.ShiftInfo s,
            Map<String, ShiftOption> optionById,
            Map<String, ShiftOption> optionByTitle
    ) {
        if (!isBlank(s.getShiftId()) && optionById.containsKey(s.getShiftId().trim())) {
            return optionById.get(s.getShiftId().trim());
        }
        if (!isBlank(s.getShiftName())) {
            return optionByTitle.get(s.getShiftName().trim().toLowerCase(Locale.ROOT));
        }
        return null;
    }

    private static String resolveShiftLocation(
            StaffWorkScheduleResponse.ShiftInfo s,
            @Nullable ShiftOption matchedOpt,
            @Nullable SharedPreferenceApp pref,
            @Nullable String companyName
    ) {
        if (!isBlank(s.getAddress())) {
            return LocationUtils.stripCountryOrLastSegment(s.getAddress().trim());
        }
        if (pref != null) {
            String exact = pref.getExactShiftAddress(s.getShiftName());
            if (!isBlank(exact)) return exact;
            if (matchedOpt != null && !isBlank(matchedOpt.getTitle())) {
                exact = pref.getExactShiftAddress(matchedOpt.getTitle());
                if (!isBlank(exact)) return exact;
            }
        }

        double[] exactCoords = findShiftCoordinates(s, matchedOpt, pref, false);
        if (exactCoords != null) {
            return LocationUtils.formatCoordinates(exactCoords[0], exactCoords[1]);
        }

        if (pref != null) {
            String latestAddr = pref.getLatestShiftAddress();
            if (!isBlank(latestAddr)) return latestAddr;

            String compAddr = pref.getCompanyAddress();
            if (!isBlank(compAddr)) return compAddr;

            double[] fallbackCoords = findShiftCoordinates(s, matchedOpt, pref, true);
            if (fallbackCoords != null) {
                return LocationUtils.formatCoordinates(fallbackCoords[0], fallbackCoords[1]);
            }
        }

        if (!isBlank(companyName)) {
            return companyName.trim();
        }
        return "Vị trí ca làm việc";
    }

    @Nullable
    private static double[] findShiftCoordinates(
            StaffWorkScheduleResponse.ShiftInfo s,
            @Nullable ShiftOption matchedOpt,
            @Nullable SharedPreferenceApp pref,
            boolean includeGlobalFallback
    ) {
        if (isValidCoords(s.getLatitude(), s.getLongitude())) {
            return new double[]{s.getLatitude(), s.getLongitude()};
        }
        if (matchedOpt != null && isValidCoords(matchedOpt.getLatitude(), matchedOpt.getLongitude())) {
            return new double[]{matchedOpt.getLatitude(), matchedOpt.getLongitude()};
        }
        if (pref != null && !isBlank(s.getShiftName())) {
            double lat = pref.getShiftLat(s.getShiftName());
            double lon = pref.getShiftLon(s.getShiftName());
            if (isValidCoords(lat, lon)) {
                return new double[]{lat, lon};
            }
        }
        if (includeGlobalFallback && pref != null) {
            double latestLat = pref.getLatestShiftLat();
            double latestLon = pref.getLatestShiftLon();
            if (isValidCoords(latestLat, latestLon)) {
                return new double[]{latestLat, latestLon};
            }
            double compLat = pref.getCompanyLat();
            double compLon = pref.getCompanyLon();
            if (isValidCoords(compLat, compLon)) {
                return new double[]{compLat, compLon};
            }
        }
        return null;
    }

    private static boolean isValidCoords(@Nullable Double lat, @Nullable Double lon) {
        return lat != null && lon != null
                && !Double.isNaN(lat) && !Double.isNaN(lon)
                && lat >= -90.0 && lat <= 90.0
                && lon >= -180.0 && lon <= 180.0
                && (lat != 0.0 || lon != 0.0);
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Xác định trạng thái của ca làm việc theo quy định:
     * - Nếu nhân viên đã check-out -> Đã hoàn thành (COMPLETED)
     * - Nếu nhân viên đã check-in nhưng chưa check-out -> Đang làm việc (WORKING)
     * - Nếu chưa check-in:
     *   + Nếu ca đã kết thúc (ngày trong quá khứ hoặc hôm nay nhưng sau giờ kết thúc) -> Vắng mặt (ABSENT)
     *   + Nếu chưa đến thời gian vào ca (ngày tương lai hoặc hôm nay trước giờ kết thúc) -> Chưa vào ca (PENDING)
     */
    public static ShiftStatus calculateShiftStatus(LocalDate date, LocalTime startTime, LocalTime endTime, CheckState attendanceState) {
        if (attendanceState == CheckState.CHECKED_OUT) {
            return ShiftStatus.COMPLETED;
        }
        if (attendanceState == CheckState.CHECKED_IN) {
            return ShiftStatus.WORKING;
        }

        if (date == null) return ShiftStatus.PENDING;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today)) {
            // Ca ngày trong quá khứ mà chưa chấm công -> Vắng mặt
            return ShiftStatus.ABSENT;
        } else if (date.isAfter(today)) {
            // Ca ngày trong tương lai -> Chưa vào ca
            return ShiftStatus.PENDING;
        } else {
            // Ca ngày hôm nay
            if (endTime != null) {
                boolean isOvernight = (startTime != null && endTime.isBefore(startTime));
                if (!isOvernight && now.isAfter(endTime)) {
                    // Ca đã kết thúc mà chưa chấm công vào -> Vắng mặt
                    return ShiftStatus.ABSENT;
                }
            }

            // Nếu đã qua giờ bắt đầu ca làm mà chưa bấm check-in -> Chưa chấm công
            if (startTime != null && now.isAfter(startTime)) {
                return ShiftStatus.NOT_CHECKED_IN;
            }

            // Chưa đến thời gian vào ca -> Chưa vào ca
            return ShiftStatus.PENDING;
        }
    }

    /** Ca hôm nay: ca đầu tiên chưa kết thúc, hết ca rồi thì lấy ca cuối. Không có ca thì null. */
    public static Shift pickTodayShift(List<Shift> shifts, LocalDate today) {
        LocalTime now = LocalTime.now();
        Shift last = null;
        for (Shift shift : shifts) { // đã sắp theo ngày rồi giờ bắt đầu
            if (!today.equals(shift.getDate())) continue;
            last = shift;
            if (shift.getEndTime() == null || shift.getEndTime().isAfter(now)) {
                return shift;
            }
        }
        return last;
    }

    /** Danh sách tất cả các ca hôm nay. Trả về rỗng nếu không có ca nào. */
    public static List<Shift> pickTodayShifts(List<Shift> shifts, LocalDate today) {
        List<Shift> result = new ArrayList<>();
        if (shifts == null) return result;
        for (Shift shift : shifts) {
            if (today.equals(shift.getDate())) {
                result.add(shift);
            }
        }
        return result;
    }

    /** Các ca sau hôm nay, tối đa limit ca. */
    public static List<Shift> upcoming(List<Shift> shifts, LocalDate today, int limit) {
        List<Shift> result = new ArrayList<>();
        for (Shift shift : shifts) {
            if (shift.getDate().isAfter(today)) {
                result.add(shift);
                if (result.size() >= limit) break;
            }
        }
        return result;
    }

    /** Các ca trong khoảng [from, toInclusive]. */
    public static List<Shift> inRange(List<Shift> shifts, LocalDate from, LocalDate toInclusive) {
        List<Shift> result = new ArrayList<>();
        for (Shift shift : shifts) {
            LocalDate d = shift.getDate();
            if (!d.isBefore(from) && !d.isAfter(toInclusive)) {
                result.add(shift);
            }
        }
        return result;
    }

    /**
     * 7 ngày của tuần bắt đầu từ weekStart.
     * Quy tắc trạng thái đồng bộ:
     * - Ngày có ca: đã chấm (PRESENT), quá hạn chưa chấm (ABSENT), chưa chấm (NOT_CHECKED)
     * - Ngày KHÔNG có ca:
     *   + Nếu là cuối tuần (T7, CN): WEEKEND
     *   + Nếu có đơn nghỉ phép: LEAVE
     *   + Còn lại: NOT_CHECKED (chứ không phải LEAVE)
     */
    public static List<WeekDay> buildWeekDays(List<Shift> shifts, LocalDate weekStart, LocalDate today) {
        return buildWeekDays(shifts, weekStart, today, null);
    }

    public static List<WeekDay> buildWeekDays(List<Shift> shifts, LocalDate weekStart, LocalDate today, SharedPreferenceApp pref) {
        Set<LocalDate> scheduledDates = new HashSet<>();
        Set<LocalDate> presentDates = new HashSet<>();

        if (shifts != null) {
            for (Shift shift : shifts) {
                if (shift == null || shift.getDate() == null) continue;
                LocalDate d = shift.getDate();
                scheduledDates.add(d);

                if (pref != null) {
                    CheckState cs = pref.getShiftAttendanceState(
                            shift.getId(), d.toString(), shift.getShiftName()
                    );
                    if (cs == CheckState.CHECKED_IN || cs == CheckState.CHECKED_OUT) {
                        presentDates.add(d);
                    }
                }
                if (shift.getStatus() == ShiftStatus.WORKING || shift.getStatus() == ShiftStatus.COMPLETED) {
                    presentDates.add(d);
                }
            }
        }

        List<WeekDay> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
            boolean hasShift = scheduledDates.contains(date);
            boolean hasAttendance = presentDates.contains(date);
            boolean isLeave = (pref != null && pref.isLeaveDate(date));

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
                } else if (isLeave) {
                    status = AttendanceStatus.LEAVE;
                } else {
                    // Những ngày chưa được xếp ca (trừ ngày làm đơn nghỉ phép, ngày cuối tuần) là NOT_CHECKED
                    status = AttendanceStatus.NOT_CHECKED;
                }
            }

            weekDays.add(new WeekDay(date, status, date.equals(today)));
        }
        return weekDays;
    }

    /** Tổng số giờ của các ca (ca qua đêm, giờ kết thúc nhỏ hơn giờ bắt đầu, được cộng thêm 24 giờ). */
    public static double totalHours(List<Shift> shifts) {
        long minutes = 0;
        for (Shift shift : shifts) {
            LocalTime start = shift.getStartTime();
            LocalTime end = shift.getEndTime();
            if (start == null || end == null) continue;
            long m = Duration.between(start, end).toMinutes();
            if (m < 0) m += 24 * 60;
            minutes += m;
        }
        return minutes / 60.0;
    }

    private static LocalDate parseDate(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s); // yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime parseTime(String s) {
        if (s == null) return null;
        try {
            return LocalTime.parse(s); // HH:mm hoặc HH:mm:ss
        } catch (Exception e) {
            return null;
        }
    }
}
