package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.profile.CreateEmployeeRequest;
import com.example.attendanceapp.data.dto.request.profile.CreateManagerRequest;
import com.example.attendanceapp.data.dto.request.profile.HandleJoinRequestRequest;
import com.example.attendanceapp.data.dto.request.profile.JoinCompanyRequest;
import com.example.attendanceapp.data.dto.request.profile.UpdateEmployeeRequest;
import com.example.attendanceapp.data.dto.request.profile.UpdateManagerRequest;
import com.example.attendanceapp.data.dto.response.auth.MessageResponse;
import com.example.attendanceapp.data.dto.response.profile.CreateEmployeeResponse;
import com.example.attendanceapp.data.dto.response.profile.CreateManagerResponse;
import com.example.attendanceapp.data.dto.response.profile.HandleJoinRequestResponse;
import com.example.attendanceapp.data.dto.response.profile.JoinCompanyPendingResponse;
import com.example.attendanceapp.data.dto.response.profile.JoinRequestsListResponse;
import com.example.attendanceapp.data.dto.response.profile.UpdateEmployeeResponse;
import com.example.attendanceapp.data.dto.response.profile.UpdateManagerResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {
    // ===== HỒ SƠ NHÂN VIÊN (STAFF) =====
    /** Lấy thông tin hồ sơ Nhân viên */
    @GET("staffs/profile")
    Single<UpdateEmployeeResponse> getStaffProfile();

    /** Tạo hồ sơ Nhân viên */
    @POST("staffs/profile")
    Single<CreateEmployeeResponse> createStaffProfile(@Body CreateEmployeeRequest request);

    /** Cập nhật hồ sơ Nhân viên */
    @PATCH("staffs/profile")
    Single<UpdateEmployeeResponse> updateStaffProfile(@Body UpdateEmployeeRequest request);


    // ===== HỒ SƠ QUẢN LÝ (MANAGER) =====
    /** Lấy thông tin hồ sơ Quản lý */
    @GET("managers/profile")
    Single<UpdateManagerResponse> getManagerProfile();

    /** Tạo hồ sơ Quản lý */
    @POST("managers/profile")
    Single<CreateManagerResponse> createManagerProfile(@Body CreateManagerRequest request);

    /** Cập nhật hồ sơ Quản lý */
    @PATCH("managers/profile")
    Single<UpdateManagerResponse> updateManagerProfile(@Body UpdateManagerRequest request);




    // ==========================================
    // ===== CHỨC NĂNG CỦA QUẢN LÝ =====
    // ==========================================

    /** Lấy mã mời hiện tại của công ty để chia sẻ cho nhân viên */
    @GET("company/invitation-code")
    Single<MessageResponse> getInvitationCode();

    /** Tạo mã mời mới (mã cũ hết hiệu lực) */
    @POST("company/invitation-code/regenerate")
    Single<MessageResponse> regenerateInvitationCode();

    /** Lấy danh sách yêu cầu tham gia công ty (truyền status = PENDING) */
    @GET("company/join-requests")
    Single<JoinRequestsListResponse> getJoinRequests(@Query("status") String status);

    /** Duyệt hoặc từ chối yêu cầu tham gia công ty */
    @PATCH("company/join-requests/{requestId}")
    Single<HandleJoinRequestResponse> handleJoinRequest(
            @Path("requestId") String requestId,
            @Body HandleJoinRequestRequest request
    );


    // ==========================================
    // ===== CHỨC NĂNG CỦA NHÂN VIÊN =====
    // ==========================================

    /** Nhân viên tham gia công ty bằng mã mời */
    @POST("company/join")
    Single<JoinCompanyPendingResponse> joinManagerWithCode(@Body JoinCompanyRequest request);
}