package com.example.attendanceapp.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AuthRepository;

import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final SharedPreferenceApp sharedPreferenceApp;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public AuthRepositoryImpl(SharedPreferenceApp sharedPreferenceApp) {
        this.sharedPreferenceApp = sharedPreferenceApp;
    }

    @Override
    public void register(String fullName, String phoneNumber, String password, RegisterCallback callback) {
        mainHandler.postDelayed(() -> {
            if (phoneNumber.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                callback.onError("Vui lòng điền đầy đủ thông tin");
                return;
            }

            // Mặc định tạo tài khoản chưa gán role cụ thể (truyền null hoặc mặc định)
            UserAccount newUser = new UserAccount(
                    UUID.randomUUID().toString(),
                    fullName,
                    phoneNumber,
                    null,           // Chưa chọn Role
                    true,           // isFirstLogin
                    true,           // isActive
                    Instant.now()
            );

            // Lưu thông tin cơ bản
            sharedPreferenceApp.saveUserInfo(newUser);
            sharedPreferenceApp.saveAuthToken("mock_jwt_token_register");
            sharedPreferenceApp.saveUserStatus("NEEDS_ROLE_SELECTION"); // Trạng thái cần chọn Role
            sharedPreferenceApp.savePhone(phoneNumber);
            sharedPreferenceApp.savePassword(password);

            callback.onSuccess(newUser);
        }, 1200);
    }

    @Override
    public void selectRole(UserRole role, RoleCallback callback) {
        mainHandler.postDelayed(() -> {
            if (role == null) {
                callback.onError("Vui lòng chọn vai trò");
                return;
            }

            // Lưu Vai trò vào SharedPreferences
            sharedPreferenceApp.setUserRole(role.getValue());
            callback.onSuccess();
        }, 800);
    }

    @Override
    public void createCompany(String companyName, CompanyCallback callback) {
        mainHandler.postDelayed(() -> {
            if (companyName.isEmpty()) {
                callback.onError("Tên công ty không được để trống");
                return;
            }

            // Quản lý hoàn thành tạo công ty -> Đã duyệt
            sharedPreferenceApp.setLoggedIn(true);
            sharedPreferenceApp.saveUserStatus("APPROVED");

            callback.onSuccess(companyName);
        }, 1000);
    }

    @Override
    public void joinCompanyWithCode(String invitationCode, CompanyCallback callback) {
        mainHandler.postDelayed(() -> {
            if (invitationCode.length() < 4) {
                callback.onError("Mã mời không hợp lệ hoặc không tồn tại");
                return;
            }

            String companyName = "Công ty Công nghệ ABC";

            // Nhân viên gửi mã thành công -> Trạng thái chờ phê duyệt
            sharedPreferenceApp.setLoggedIn(true);
            sharedPreferenceApp.saveUserStatus("PENDING_APPROVAL");

            callback.onSuccess(companyName);
        }, 1000);
    }

    @Override
    public void login(String phoneNumber, String password, LoginCallback callback) {
        mainHandler.postDelayed(() -> {
            // Verify từ SharedPreferences thay vì hardcode
            String savedPhone = sharedPreferenceApp.getPhone();
            String savedPassword = sharedPreferenceApp.getPassword();

            if (phoneNumber.equals(savedPhone) && password.equals(savedPassword)) {
                // Đọc xem tài khoản này trước đó đã chọn role chưa
                String savedRoleStr = sharedPreferenceApp.getUserRole();
                UserRole role = UserRole.fromString(savedRoleStr);

                // Lấy userId đã lưu
                String savedUserId = sharedPreferenceApp.getUserId();

                UserAccount user = new UserAccount(
                        savedUserId != null ? savedUserId : UUID.randomUUID().toString(),
                        savedUserId != null ? "User" : phoneNumber, // Tên giả lập nếu chưa lưu
                        phoneNumber,
                        role,
                        false,
                        true,
                        Instant.now()
                );

                sharedPreferenceApp.setLoggedIn(true);
                sharedPreferenceApp.saveAuthToken("mock_jwt_token_login");
                sharedPreferenceApp.saveUserInfo(user);

                callback.onSuccess(user);
            } else {
                callback.onError("Số điện thoại hoặc mật khẩu không chính xác");
            }
        }, 1500);
    }

    @Override
    public void sendOtp(String phoneNumber, OtpCallback callback) {
        mainHandler.postDelayed(() -> {
            if (phoneNumber.length() < 10) {
                callback.onError("Số điện thoại không hợp lệ");
            } else {
                callback.onSuccess();
            }
        }, 1000);
    }

    @Override
    public void verifyOtp(String phoneNumber, String otp, OtpCallback callback) {
        mainHandler.postDelayed(() -> {
            if ("123456".equals(otp)) {
                callback.onSuccess();
            } else {
                callback.onError("Mã OTP không đúng hoặc đã hết hạn");
            }
        }, 1000);
    }

    @Override
    public void resetPassword(String phoneNumber, String newPassword, ResetPasswordCallback callback) {
        mainHandler.postDelayed(() -> {
            if (newPassword.length() < 6) {
                callback.onError("Mật khẩu phải chứa ít nhất 6 ký tự");
            } else {
                callback.onSuccess();
            }
        }, 1200);
    }

    @Override
    public void logout() {
        sharedPreferenceApp.clearSession();
    }
}