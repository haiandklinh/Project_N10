package com.example.attendanceapp.data.mapper;


import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.model.WeekDay;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chuyển lịch làm việc của nhân viên từ server (mỗi ngày kèm danh sách ca) sang model của UI.
 * Dùng chung cho màn Trang chủ và màn Lịch.
 */
public final class StaffScheduleMapper {

    private StaffScheduleMapper() { }

    /**
     * Tách mỗi (ngày, ca) thành một Shift, sắp theo ngày rồi giờ bắt đầu.
     * Server chưa trả địa điểm và chưa có API chấm công nên location để rỗng, status là PENDING.
     * Lịch này là của chính người đăng nhập nên userId để null.
     */
    public static List<Shift> toShifts(List<StaffWorkScheduleResponse> schedules) {
        return toShifts(schedules, null);
    }

    public static List<Shift> toShifts(List<StaffWorkScheduleResponse> schedules, SharedPreferenceApp pref) {
        List<Shift> result = new ArrayList<>();
        if (schedules == null) return result;

        for (StaffWorkScheduleResponse item : schedules) {
            if (item == null || item.getShifts() == null) continue;

            LocalDate date = parseDate(item.getWorkDay());
            if (date == null) continue;

            for (StaffWorkScheduleResponse.ShiftInfo s : item.getShifts()) {
                if (s == null) continue;
                String location = "";
                if (pref != null) {
                    location = pref.getShiftAddress(s.getShiftName());
                }
                if (location == null || location.trim().isEmpty()) {
                    if (pref != null) {
                        location = pref.getLatestShiftAddress();
                        if (location == null || location.trim().isEmpty()) {
                            location = pref.getCompanyAddress();
                        }
                    }
                }
                if (location == null || location.trim().isEmpty()) {
                    location = "Vị trí ca làm việc";
                }

                LocalTime startTime = parseTime(s.getStartTime());
                LocalTime endTime = parseTime(s.getEndTime());
                LocalTime allowedCheckIn = parseTime(s.getAllowedCheckInTime());
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
                        null,
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
     * 7 ngày của tuần bắt đầu từ weekStart. Chưa có API chấm công nên chưa biết đi làm hay vắng:
     * ngày thường là NOT_CHECKED, Thứ 7/Chủ nhật không có ca là WEEKEND.
     */
    public static List<WeekDay> buildWeekDays(List<Shift> shifts, LocalDate weekStart, LocalDate today) {
        Set<LocalDate> scheduledDates = new HashSet<>();
        for (Shift shift : shifts) {
            scheduledDates.add(shift.getDate());
        }

        List<WeekDay> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            AttendanceStatus status = (weekend && !scheduledDates.contains(date))
                    ? AttendanceStatus.WEEKEND
                    : AttendanceStatus.NOT_CHECKED;
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