package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.staff.AttendanceAdjustmentRequest;
import com.example.attendanceapp.data.dto.request.staff.ChangeShiftRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateLeaveRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateStaffProfileRequest;
import com.example.attendanceapp.data.dto.request.staff.JoinCompanyRequest;
import com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.staff.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffCompanyResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffProfileResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StaffApi {

    @GET("staffs/profile")
    Single<StaffProfileResponse> getStaffProfile();

    @POST("staffs/profile")
    Single<StaffProfileResponse> createStaffProfile(@Body CreateStaffProfileRequest request);

    @PATCH("staffs/profile")
    Single<StaffProfileResponse> updateStaffProfile(@Body UpdateStaffProfileRequest request);

    @GET("staffs/invitation_code")
    Single<ResponseBody> getManagerIdByInvitationCode(@Query("invitation_code") String invitationCode);

    @POST("staffs/join-requests")
    Single<ResponseBody> sendJoinRequest(@Body JoinCompanyRequest request);

    // GET /staffs/my-company - Get Companies joined by Staff
    @GET("staffs/my-company")
    Single<BaseListResponse<StaffCompanyResponse>> getMyCompanies();

    @GET("staffs/me/work-schedules")
    Single<BaseListResponse<StaffWorkScheduleResponse>> getWorkSchedules(@Query("id_manager") String managerId);

    @GET("staffs/me/notifications")
    Single<BaseListResponse<NotificationItemResponse>> getNotifications(@Query("id_manager") String managerId);

    @POST("staffs/work-schedules-requests")
    Single<ResponseBody> requestChangeShift(
            @Query("id_manager") String managerId,
            @Body ChangeShiftRequest request
    );

    @POST("staffs/leave-requests")
    Single<ResponseBody> requestLeave(
            @Query("id_manager") String managerId,
            @Body CreateLeaveRequest request
    );

    @POST("staffs/attendance-adjustment-requests")
    Single<ResponseBody> requestAttendanceAdjustment(
            @Query("id_manager") String managerId,
            @Body AttendanceAdjustmentRequest request
    );
}
