package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.data.dto.request.manager.CreateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleJoinRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleLeaveRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleShiftChangeRequest;
import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.CompanyInvitationResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.domain.model.UserAccount;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/** Tất cả nghiệp vụ gọi ManagerApi. */
public interface ManagerRepository {

    /** Lỗi AppException code PROFILE_NOT_FOUND nếu chưa có hồ sơ Quản lý. Thành công thì tự lưu Room + AppState. */
    Single<UserAccount> getProfile();

    Single<UserAccount> createProfile(CreateManagerProfileRequest request);

    Single<UserAccount> updateProfile(UpdateManagerProfileRequest request);

    /** Lấy mã mời (ưu tiên cache Room, không có thì gọi server). */
    Single<String> getInvitationCode();

    Single<CompanyInvitationResponse> generateInvitationCode(int expiresIn);

    Single<BaseListResponse<NotificationItemResponse>> getNotifications();

    Completable handleShiftChange(HandleShiftChangeRequest request);

    Completable handleLeaveRequest(HandleLeaveRequest request);

    Completable handleJoinRequest(HandleJoinRequest request);

    Single<BaseListResponse<StaffItemResponse>> getStaffs();

    Single<okhttp3.ResponseBody> getApprovalRequests();

    Single<okhttp3.ResponseBody> getEmployees();

    Single<okhttp3.ResponseBody> getPayrollDetail();
}
