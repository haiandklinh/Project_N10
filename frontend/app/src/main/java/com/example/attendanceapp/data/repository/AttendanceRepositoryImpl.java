package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.attendance.CheckInRequest;
import com.example.attendanceapp.data.dto.request.attendance.CheckOutRequest;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceMessageResponse;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordPageResponse;
import com.example.attendanceapp.data.remote.AttendanceApi;
import com.example.attendanceapp.domain.repository.AttendanceRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

@Singleton
public class AttendanceRepositoryImpl implements AttendanceRepository {

    private final AttendanceApi attendanceApi;

    @Inject
    public AttendanceRepositoryImpl(AttendanceApi attendanceApi) {
        this.attendanceApi = attendanceApi;
    }

    @Override
    public Single<AttendanceMessageResponse> checkIn(CheckInRequest request) {
        return attendanceApi.checkIn(request);
    }

    @Override
    public Single<AttendanceMessageResponse> checkOut(CheckOutRequest request) {
        return attendanceApi.checkOut(request);
    }

    @Override
    public Single<AttendanceRecordPageResponse> getAttendanceRecords(
            String managerId,
            String startDate,
            String endDate,
            Integer page,
            Integer size
    ) {
        return attendanceApi.getAttendanceRecords(managerId, startDate, endDate, page, size);
    }
}