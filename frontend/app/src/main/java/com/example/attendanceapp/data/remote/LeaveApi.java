package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.leave.CreateLeaveRequest;
import com.example.attendanceapp.data.dto.response.auth.MessageResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LeaveApi {
    /**
     * Gửi yêu cầu xin nghỉ phép (Nhân viên)
     */
    @POST("leave-requests")
    Single<MessageResponse> createLeaveRequest(@Body CreateLeaveRequest request);
}
