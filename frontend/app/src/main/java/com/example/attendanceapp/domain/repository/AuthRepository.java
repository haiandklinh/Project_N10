package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;

/**
 * Chỉ chịu trách nhiệm XÁC THỰC (gọi Database/API).
 * Không lưu trữ session local — việc đó do UserRepository đảm nhiệm.
 */
public interface AuthRepository {

    // =========================================================
    // CALLBACK DÙNG CHUNG CHO MỌI THAO TÁC BẤT ĐỒNG BỘ
    // =========================================================
    interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    // =========================================================
    // ĐĂNG KÝ / ĐĂNG NHẬP
    // =========================================================

    /** 1. Đăng ký chỉ cần Họ tên, SĐT, Mật khẩu (chưa có role) */
    void register(String fullName, String phoneNumber, String password, DataCallback<UserAccount> callback);

    /** Kiểm tra số điện thoại đã tồn tại hay chưa (dùng khi đăng ký) */
    void checkPhoneExists(String phoneNumber, DataCallback<Boolean> callback);

    /** 2. Đăng nhập */
    void login(String phoneNumber, String password, DataCallback<UserAccount> callback);

    /** 3. Chọn/cập nhật vai trò sau khi đăng nhập/đăng ký thành công */
    void selectRole(UserRole role, DataCallback<Void> callback);

    // =========================================================
    // XỬ LÝ CÔNG TY THEO VAI TRÒ
    // =========================================================

    void createCompany(String companyName, DataCallback<String> callback);

    void joinCompanyWithCode(String invitationCode, DataCallback<String> callback);

    // =========================================================
    // OTP & QUÊN MẬT KHẨU
    // =========================================================

    void sendOtp(String phoneNumber, DataCallback<Void> callback);

    void verifyOtp(String phoneNumber, String otp, DataCallback<Void> callback);

    void resetPassword(String phoneNumber, String newPassword, DataCallback<Void> callback);

    /** Đăng xuất - có callback để gọi API invalidate token nếu cần */
    void logout(DataCallback<Void> callback);
}