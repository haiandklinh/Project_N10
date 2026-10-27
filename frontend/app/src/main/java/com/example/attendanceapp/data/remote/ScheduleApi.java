package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.schedule.AssignWorkScheduleRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.schedule.EmployeeWorkScheduleResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ScheduleApi {

    /** 1. GET /work-schedules - Get List Staff Work Schedule */
    @GET("work-schedules")
    Single<BaseListResponse<EmployeeWorkScheduleResponse>> getListStaffWorkSchedule();

    /** 2. POST /work-schedules - Assign Work Schedules (1 nhân viên, 1 ngày, nhiều ca) */
    @POST("work-schedules")
    Single<ResponseBody> assignWorkSchedule(
            @Body AssignWorkScheduleRequest request
    );
}