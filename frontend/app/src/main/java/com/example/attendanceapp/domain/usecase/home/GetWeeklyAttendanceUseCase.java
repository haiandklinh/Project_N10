package com.example.attendanceapp.domain.usecase.home;

import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.domain.model.WeekDay;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class GetWeeklyAttendanceUseCase {

    @Inject
    public GetWeeklyAttendanceUseCase() {

    }

    public List<WeekDay> execute(LocalDate currentDate, List<RecentAttendance> attendances) {
        List<WeekDay> weekDays = new ArrayList<>();
        LocalDate monday = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);

        // Convert list thành map
        // 1. Khởi tạo một Map rỗng:
        // Key (Khóa) = LocalDate (Ngày)
        // Value (Giá trị) = RecentAttendance (Thông tin chấm công của ngày đó)
        Map<LocalDate, RecentAttendance> attendanceMap = new HashMap<>();

        // 2. Kiểm tra an toàn: Nếu danh sách không bị null thì mới làm tiếp
        if(attendances != null) {
            // 3. Duyệt qua từng mục Chấm công trong danh sách List truyền vào
            for (RecentAttendance item : attendances) {
                // 4. Kiểm tra xem ngày của mục đó có hợp lệ (không null) không
                if (item.getDate() != null) {
                    // 5. CẤU TRÚC CỐT LÕI: Đưa dữ liệu vào Map
                    // Cất đối tượng `item` vào Map với chìa khóa tra cứu chính là `item.getDate()`
                    attendanceMap.put(item.getDate(), item);
                }
            }
        }
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            AttendanceStatus status = getStatusForDate(date, currentDate, attendanceMap);
            weekDays.add(new WeekDay(date, status, date.equals(currentDate)));
        }
        return weekDays;
    }

    private AttendanceStatus getStatusForDate(LocalDate date, LocalDate today, Map<LocalDate, RecentAttendance> attendanceMap) {
        if (date.isAfter(today)) {
            return AttendanceStatus.NOT_CHECKED;
        }

        RecentAttendance attendance = attendanceMap.get(date);
        if (attendance != null && attendance.getStatus() != null) {
            return attendance.getStatus();
        }

        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        if (isWeekend) {
            return AttendanceStatus.WEEKEND;
        }

        return date.equals(today) ? AttendanceStatus.NOT_CHECKED : AttendanceStatus.ABSENT;
    }
}