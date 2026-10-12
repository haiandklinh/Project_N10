package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.OtpPurpose;
import com.example.attendanceapp.domain.model.UserAccount;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/** Chỉ lo xác thực. Hồ sơ, vai trò, công ty nằm ở UserRepository. */
public interface AuthRepository {

    Single<Boolean> checkPhoneExists(String phoneNumber);

    Completable register(String phoneNumber, String password);

    /** Đăng nhập, lưu token, lấy thông tin hiện tại và lưu phiên. status của tài khoản quyết định màn tiếp theo. */
    Single<UserAccount> login(String phoneNumber, String password);

    Completable logout();

    Single<String> sendOtp(String phoneNumber, OtpPurpose purpose);

    Single<String> verifyOtp(String phoneNumber, String otp, OtpPurpose purpose);

    Completable resetPassword(String phoneNumber, String newPassword);
}