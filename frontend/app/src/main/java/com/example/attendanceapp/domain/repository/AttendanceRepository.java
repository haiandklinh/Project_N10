package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.domain.model.Shift;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface AttendanceRepository {

    // =====================================================================
    // 1. TRUY VẤN DỮ LIỆU (QUERY) - Dùng Observable để tự động phát hiện thay đổi DB
    // =====================================================================

    /** Lấy ca làm việc của một ngày cụ thể */
    Observable<Shift> getShiftByDate(LocalDate date);

    /** Lấy danh sách lịch sử chấm công gần đây */
    Observable<List<RecentAttendance>> getRecentAttendances(int limit);

    /** Lấy trạng thái chấm công của các ngày trong tuần (Dùng cho dải 7 ngày ở Home) */
    Single<List<RecentAttendance>> getWeeklyAttendanceStatus(LocalDate startOfWeek, LocalDate endOfWeek);


    // =====================================================================
    // 2. THAO TÁC NGHIỆP VỤ (COMMAND / MUTATION)
    // =====================================================================

    /** Thực hiện Check-in / Check-out */
    Completable performCheckInOrOut(LocalDate date, LocalTime time, double latitude, double longitude);

    /** Gửi yêu cầu giải trình / chỉnh sửa chấm công */
    Completable submitAttendanceAdjustment(LocalDate date, String reason);
}