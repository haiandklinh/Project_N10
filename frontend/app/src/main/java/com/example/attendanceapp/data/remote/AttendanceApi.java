package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.attendance.CheckInRequest;
import com.example.attendanceapp.data.dto.request.attendance.CheckOutRequest;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceMessageResponse;
import com.example.attendanceapp.data.dto.response.attendance.AttendanceRecordPageResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AttendanceApi {

    // 1. POST /attendances/check-in - Check In
    @POST("attendances/check-in")
    Single<AttendanceMessageResponse> checkIn(
            @Body CheckInRequest request
    );

    // 2. PATCH /attendances/check-out - Check Out
    @PATCH("attendances/check-out")
    Single<AttendanceMessageResponse> checkOut(
            @Body CheckOutRequest request
    );

    // 3. GET /attendances - Get Attendance Record
    @GET("attendances")
    Single<AttendanceRecordPageResponse> getAttendanceRecords(
            @Query("id_manager") String managerId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("page") Integer page,
            @Query("size") Integer size
    );
}