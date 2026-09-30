package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.profile.ProfileRequest;
import com.example.attendanceapp.data.dto.request.profile.HandleJoinRequestRequest;
import com.example.attendanceapp.data.dto.request.profile.JoinCompanyRequest;
import com.example.attendanceapp.data.dto.response.profile.HandleJoinRequestResponse;
import com.example.attendanceapp.data.dto.response.profile.JoinCompanyManagerResponse;
import com.example.attendanceapp.data.dto.response.profile.JoinRequestsListResponse;
import com.example.attendanceapp.data.dto.response.auth.MessageResponse;
import com.example.attendanceapp.data.dto.response.profile.ProfileResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {

    // ===== Cả hai vai trò =====
    // Lấy thông tin hồ sơ (Dùng chung cho cả Manager/Staff tùy theo Token)
    @GET("profile")
    Single<ProfileResponse> getProfile();
    /**
     * Tạo hồ sơ cá nhân (POST /profile).
     * Token được gắn tự động qua AuthInterceptor, không cần truyền thủ công ở Tham số.
     */
    @Multipart
    @POST("profile")
    Single<ProfileResponse> createProfile(
            @Part("full_name") okhttp3.RequestBody fullName,
            @Part("date") okhttp3.RequestBody date,
            @Part("gender") okhttp3.RequestBody gender,
            @Part okhttp3.MultipartBody.Part avatar
    );

    @POST("profile")
    Single<ProfileResponse> createProfile(@Body ProfileRequest request);

    /** Cập nhật hồ sơ cá nhân sau này */
    @PATCH("profile")
    Single<ProfileResponse> updateProfile(@Body ProfileRequest request);

    /** Thông tin hiện tại: dùng khi mở app và ở trang cá nhân (GET /me) */
    @GET("me")
    Single<ProfileResponse> getMe();


    @Multipart
    @POST("uploads-avatar")
    Single<MessageResponse> uploadAvatar(@Part MultipartBody.Part file);


    // ===== Dành cho Quản lý =====

    /** Lấy mã mời hiện tại của công ty để chia sẻ cho nhân viên. */
    @GET("company/invitation-code")
    Single<MessageResponse> getInvitationCode();

    /** Tạo mã mời mới (mã cũ hết hiệu lực) */
    @POST("company/invitation-code/regenerate")
    Single<MessageResponse> regenerateInvitationCode();

    /** status = PENDING để lấy danh sách chờ duyệt. */
    @GET("company/join-requests")
    Single<JoinRequestsListResponse> getJoinRequests(@Query("status") String status);

    /** Duyệt hoặc từ chối yêu cầu tham gia công ty */
    @PATCH("company/join-requests/{requestId}")
    Single<HandleJoinRequestResponse> handleJoinRequest(
            @Path("requestId") String requestId,
            @Body HandleJoinRequestRequest request);

    // ===== Dành cho Nhân viên =====

    /** Nhân viên tham gia công ty bằng mã mời */
    @POST("company/join")
    Single<JoinCompanyManagerResponse> joinManagerWithCode(@Body JoinCompanyRequest request);
}