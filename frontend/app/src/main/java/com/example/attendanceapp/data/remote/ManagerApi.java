package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.manager.CalculatePayrollRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateBonusRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateDeductionRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleJoinRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleLeaveRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleShiftChangeRequest;
import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.CalculatePayrollResponse;
import com.example.attendanceapp.data.dto.response.manager.CompanyInvitationResponse;
import com.example.attendanceapp.data.dto.response.manager.CreateBonusResponse;
import com.example.attendanceapp.data.dto.response.manager.CreateDeductionResponse;
import com.example.attendanceapp.data.dto.response.manager.ManagerProfileResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffPayrollSummaryResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    // 9. GET /managers/me/staffs - Get Staffs in Company
    @GET("managers/me/staffs")
    Single<BaseListResponse<StaffItemResponse>> getStaffs();

    // 10. GET /managers/approval-requests - Get Approval Requests
    @GET("managers/approval-requests")
    Single<ResponseBody> getApprovalRequests();

    // 13. GET /managers/me/staffs/{id_staff}/payroll-summary - Get Staff Payroll Summary
    @GET("managers/me/staffs/{id_staff}/payroll-summary")
    Single<StaffPayrollSummaryResponse> getStaffPayrollSummary(
            @Path("id_staff") String staffId
    );

    // 14. POST /managers/me/payrolls - Calculate Payroll
    @POST("managers/me/payrolls")
    Single<CalculatePayrollResponse> calculatePayroll(
            @Body CalculatePayrollRequest request
    );

    // 15. GET /managers/me/payrolls - Get Payroll History By Manager
    @GET("managers/me/payrolls")
    Single<BaseListResponse<CalculatePayrollResponse>> getPayrollHistory(
            @Query("id_staff") String staffId
    );

    // 16. POST /managers/me/bonuses - Create Bonus
    @POST("managers/me/bonuses")
    Single<CreateBonusResponse> createBonus(
            @Body CreateBonusRequest request
    );

    // 17. POST /managers/me/deductions - Create Deduction
    @POST("managers/me/deductions")
    Single<CreateDeductionResponse> createDeduction(
            @Body CreateDeductionRequest request
    );
}
