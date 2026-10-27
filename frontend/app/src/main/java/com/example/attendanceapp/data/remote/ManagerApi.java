package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.manager.CreateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleJoinRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleLeaveRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleShiftChangeRequest;
import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.CompanyInvitationResponse;
import com.example.attendanceapp.data.dto.response.manager.ManagerProfileResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ManagerApi {

    // 1. GET /managers/profile - Get Profile
    @GET("managers/profile")
    Single<ManagerProfileResponse> getManagerProfile();

    // 2. POST /managers/profile - Create Profile
    @POST("managers/profile")
    Single<ManagerProfileResponse> createManagerProfile(
            @Body CreateManagerProfileRequest request
    );

    // 3. PATCH /managers/profile - Update Profile
    @PATCH("managers/profile")
    Single<ManagerProfileResponse> updateManagerProfile(
            @Body UpdateManagerProfileRequest request
    );

    // 4. GET /managers/me/notifications - Get Notification
    @GET("managers/me/notifications")
    Single<BaseListResponse<NotificationItemResponse>> getNotifications();

    // 5. GET /managers/invitation-code - Get Invitation Code
    @GET("managers/invitation-code")
    Single<CompanyInvitationResponse> getInvitationCode(@Query("expires_in") int expiresIn);

    // 6. PATCH /managers/shift-change-requests - Handle Shift Change Request
    @PATCH("managers/shift-change-requests")
    Single<ResponseBody> handleShiftChange(
            @Body HandleShiftChangeRequest request
    );

    // 7. PATCH /managers/leave-requests - Handle Leave Request
    @PATCH("managers/leave-requests")
    Single<ResponseBody> handleLeaveRequest(
            @Body HandleLeaveRequest request
    );

    // 8. PATCH /managers/join-requests - Handle Join Request
    @PATCH("managers/join-requests")
    Single<ResponseBody> handleJoinRequest(
            @Body HandleJoinRequest request
    );

    // 9. GET /managers/staffs - Get Staffs in Company
    @GET("managers/staffs")
    Single<BaseListResponse<StaffItemResponse>> getStaffs();

    // 10. GET /managers/approval-requests - Get Approval Requests
    @GET("managers/approval-requests")
    Single<ResponseBody> getApprovalRequests();

    // 11. GET /managers/employees - Get All Employees (legacy)
    @GET("managers/employees")
    Single<ResponseBody> getEmployees();

    // 12. GET /managers/payroll-detail - Get Payroll Detail
    @GET("managers/payroll-detail")
    Single<ResponseBody> getPayrollDetail();
}
