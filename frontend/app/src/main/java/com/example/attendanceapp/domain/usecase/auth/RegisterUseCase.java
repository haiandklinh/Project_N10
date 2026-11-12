package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.OtpPurpose;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class RegisterUseCase {
    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;

    @Inject
    public RegisterUseCase(AuthRepository authRepository, AppStateRepository appStateRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa
     */
    public Single<Boolean> checkPhoneExists(String phoneNumber) {
        return authRepository.checkPhoneExists(phoneNumber);
    }

    /**
     * Gửi mã OTP về Số điện thoại
     */
    public Single<String> sendOtp(String phoneNumber) {
        return authRepository.sendOtp(phoneNumber, OtpPurpose.REGISTER);
    }

    /**
     * Xác thực mã OTP cho Đăng ký
     */
    public Single<String> verifyOtp(String account, String otp) {
        return authRepository.verifyOtp(account, otp, OtpPurpose.REGISTER);
    }

    /**
     * Đăng ký tài khoản
     * Lưu ý: Không lưu vào Room DB ở đây vì user chưa có profile đầy đủ (tên, công ty)
     * Thông tin đầy đủ sẽ được lưu khi user login lần đầu (API getMe)
     *
     * FIX Bug 3: Sau đăng ký chỉ đánh dấu isAccountSaved = true.
     * KHÔNG lưu PENDING_APPROVAL vì người dùng vừa tạo tài khoản, chưa gửi join request
     * vào công ty nào — trạng thái sẽ được xác định chính xác sau khi login và load profile.
     */
    public Completable execute(String phoneNumber, String password) {
        return authRepository.register(phoneNumber, password)
                .doOnComplete(() -> {
                    // Chỉ đánh dấu đã lưu tài khoản, không gán trạng thái PENDING_APPROVAL
                    // vì người dùng chưa tham gia doanh nghiệp nào tại thời điểm này.
                    // Trạng thái thực sẽ được xác định ở LoginUseCase khi load profile sau đăng nhập.
                    appStateRepository.setAccountSaved(true);
                    appStateRepository.saveUserStatus(UserStatus.NEEDS_ROLE_SELECTION);
                });
    }
}
