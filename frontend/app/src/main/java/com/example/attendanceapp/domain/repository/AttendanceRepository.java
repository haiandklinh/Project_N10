package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.data.dto.request.attendance.CheckInRequest;
import com.example.attendanceapp.data.dto.request.attendance.CheckOutRequest;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceMessageResponse;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordPageResponse;

import io.reactivex.rxjava3.core.Single;

public interface AttendanceRepository {

    /** Thực hiện Check-in điểm danh */
    Single<AttendanceMessageResponse> checkIn(CheckInRequest request);

    /** Thực hiện Check-out điểm danh */
    Single<AttendanceMessageResponse> checkOut(CheckOutRequest request);

    /** Lấy danh sách lịch sử điểm danh phân trang */
    Single<AttendanceRecordPageResponse> getAttendanceRecords(
            String managerId,
            String startDate,
            String endDate,
            Integer page,
            Integer size
    );
}