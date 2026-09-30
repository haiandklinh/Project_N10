package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.ManagerInfo;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;

import java.time.LocalDate;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface UserRepository {

    // ===== Bộ nhớ cục bộ (phiên đăng nhập) =====

    /** Vai trò đã lưu. Rỗng (empty) nếu chưa chọn vai trò. */
    Maybe<UserRole> getSavedUserRole();

    /** Lưu vai trò vào bộ nhớ cục bộ. */
    Completable saveUserRole(UserRole role);

    /** Tài khoản đang đăng nhập. Rỗng (empty) nếu chưa có phiên. */
    Maybe<UserAccount> getCurrentUser();

    /** Quan sát tài khoản đang đăng nhập hiện tại (phát lại mỗi khi có thay đổi). */
    Observable<UserAccount> observeCurrentUser();

    /** Lưu phiên đăng nhập (lưu xong nghĩa là đã đăng nhập). */
    Completable saveUserSession(UserAccount userAccount);

    /** Kiểm tra đã đăng nhập hay chưa. */
    Single<Boolean> isLoggedIn();

    /** Phát trạng thái đăng nhập mỗi khi thay đổi (dùng cho splash, tự chuyển màn hình). */
    Observable<Boolean> observeLoggedIn();

    /** Xóa sạch phiên làm việc khi đăng xuất. */
    Completable clearUserSession();

    // ===== Hồ sơ người dùng (gọi server) =====

    /** Chọn hoặc cập nhật vai trò trên server. */
    Completable selectRole(UserRole role);

    /** Cập nhật thông tin cá nhân. */
    Completable updatePersonalInfo(String fullName, java.time.LocalDate dob, com.example.attendanceapp.domain.model.Gender gender, android.net.Uri avatarUri, com.example.attendanceapp.domain.model.UserRole role);

    // ===== Dành cho Quản lý =====

    /** Đặt hoặc đổi tên công ty của quản lý. */
    Completable updateCompanyName(String companyName);

    /** Lấy mã mời hiện tại để chia sẻ cho nhân viên. */
    Single<String> getInvitationCode();

    /** Tạo mã mời mới (mã cũ hết hiệu lực). */
    Single<String> regenerateInvitationCode();

    // ===== Dành cho Nhân viên =====

    /** Nhập mã mời để tham gia quản lý. Trả về thông tin quản lý để hiển thị xác nhận. */
    Single<ManagerInfo> joinManagerWithCode(String otp, String name, String avatar);
}