package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.work.WorkScheduleChangeRequest;
import com.example.attendanceapp.data.dto.response.auth.MessageResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import java.util.Map;

public interface WorkScheduleChangeApi {

    /**
     * Nhân viên gửi yêu cầu đổi ca
     */
    @POST("work-schedules-requests")
    Single<MessageResponse> requestChange(@Body WorkScheduleChangeRequest request);

    /**
     * Quản lý phê duyệt/từ chối yêu cầu đổi ca
     * Body: { "status": "APPROVED" | "REJECTED" }
     */
    @PATCH("work-schedules/change-request/{requestId}")
    Single<MessageResponse> handleWorkScheduleChange(
            @Path("requestId") String requestId,
            @Body Map<String, Object> request
    );
}
