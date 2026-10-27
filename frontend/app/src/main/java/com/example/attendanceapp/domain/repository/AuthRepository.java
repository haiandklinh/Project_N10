package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.OtpPurpose;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Chỉ lo xác thực (gọi AuthApi + lưu token).
 * Hồ sơ nằm ở StaffRepository / ManagerRepository, phiên cục bộ nằm ở SessionRepository.
 */
public interface AuthRepository {

    Single<Boolean> checkPhoneExists(String phoneNumber);

    Completable register(String phoneNumber, String password);

    /** Đăng nhập, lưu token, bật cờ đã đăng nhập. KHÔNG lấy profile (profile lấy sau khi chọn Role). */
    Completable login(String phoneNumber, String password);

    Completable logout();

    Single<String> sendOtp(String phoneNumber, OtpPurpose purpose);

    Single<String> verifyOtp(String phoneNumber, String otp, OtpPurpose purpose);

    Completable resetPassword(String phoneNumber, String newPassword);
}