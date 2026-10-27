package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.data.dto.request.staff.AttendanceAdjustmentRequest;
import com.example.attendanceapp.data.dto.request.staff.ChangeShiftRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateLeaveRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateStaffProfileRequest;
import com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.staff.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffCompanyResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.model.ManagerInfo;
import com.example.attendanceapp.domain.model.UserAccount;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/** Tất cả nghiệp vụ gọi StaffApi. */
public interface StaffRepository {

    /** Lỗi AppException code PROFILE_NOT_FOUND nếu chưa có hồ sơ Nhân viên. Thành công thì tự lưu Room + AppState. */
    Single<UserAccount> getProfile();

    Single<UserAccount> createProfile(CreateStaffProfileRequest request);

    Single<UserAccount> updateProfile(UpdateStaffProfileRequest request);

    /** Bước 1: mã mời -> id quản lý. Bước 2: gửi yêu cầu tham gia. */
    Single<ManagerInfo> joinWithInvitationCode(String invitationCode, String name, String avatar);

    Single<Boolean> checkCompanyApprovalStatus(String invitationCode);

    Single<BaseListResponse<StaffCompanyResponse>> getMyCompanies();

    Single<BaseListResponse<StaffWorkScheduleResponse>> getWorkSchedules();

    Single<BaseListResponse<NotificationItemResponse>> getNotifications();

    Completable requestChangeShift(ChangeShiftRequest request);

    Completable requestLeave(CreateLeaveRequest request);

    Completable requestAttendanceAdjustment(String managerId, AttendanceAdjustmentRequest request);
}
