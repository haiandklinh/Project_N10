package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.work.AssignWorkScheduleRequest;
import com.example.attendanceapp.data.dto.response.schedule.EmployeeWorkScheduleResponse;
import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ScheduleApi {

    // ===== Dành cho Quản lý =====

    /** Lấy toàn bộ danh sách nhân viên và lịch làm việc */
    @GET("manager/work-schedules")
    Single<List<EmployeeWorkScheduleResponse>> getEmployeeWorkSchedules();

    /** Xếp ca làm và lịch làm cho nhân viên (Hàng loạt) */
    @POST("manager/work-schedules/assign")
    Single<List<EmployeeWorkScheduleResponse>> assignWorkSchedule(@Body AssignWorkScheduleRequest request);

    // ===== Dành cho Nhân viên =====

    /** Lấy ra lịch làm việc của nhân viên */
    @GET("me/work-schedules")
    Single<List<StaffWorkScheduleResponse>> getStaffWorkSchedules(@Query("date") String date);
}
