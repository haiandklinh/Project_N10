package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.data.dto.response.manager.ManagerProfileResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffProfileResponse;
import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserAccount;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/** Chỉ làm việc với Room (hồ sơ đang đăng nhập). Không gọi API. */
public interface SessionRepository {

    /** Lưu số điện thoại vào Room (sử dụng khi login). */
    Completable savePhoneNumber(String phoneNumber);

    /** Lưu/ghi đè hồ sơ Nhân viên vào Room, trả về tài khoản đã map. */
    Single<UserAccount> saveStaffProfile(StaffProfileResponse profile);

    /** Lưu/ghi đè hồ sơ Quản lý vào Room, trả về tài khoản đã map. */
    Single<UserAccount> saveManagerProfile(ManagerProfileResponse profile);

    /** Tài khoản hiện tại. Rỗng nếu chưa có. */
    Maybe<UserAccount> getCurrentUser();

    /** Phát lại mỗi khi hồ sơ thay đổi. */
    Observable<UserAccount> observeCurrentUser();

    Maybe<String> getCachedInvitationCode();

    Completable saveInvitationCode(String code);

    Completable savePendingJoinRequest(String invitationCode, String companyName);

    Completable saveApprovalSuccess(String managerId);

    Completable saveSelectedCompany(String companyId, String companyName, String position);

    Maybe<SavedAccountData> getSavedAccountData();

    /** Xóa toàn bộ dữ liệu người dùng trong Room. */
    Completable clear();
}
