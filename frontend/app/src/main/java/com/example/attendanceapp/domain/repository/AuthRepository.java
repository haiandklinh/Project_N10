package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
public interface AuthRepository {

    // CALLBACK DÙNG CHUNG CHO MỌI THAO TÁC BẤT ĐỒNG BỘ
    interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
    enum OtpPurpose { REGISTER, RESET_PASSWORD, FORGOT_PASSWORD }


    void register(String phoneNumber, String password, DataCallback<Void> callback);

    void checkPhoneExists(String phoneNumber, DataCallback<Boolean> callback);

    void login(String phoneNumber, String password, DataCallback<UserAccount> callback);

    void sendOtp(String phoneNumber, OtpPurpose purpose, DataCallback<Void> callback);

    void verifyOtp(String phoneNumber, String otp, OtpPurpose purpose, DataCallback<String> callback);

    void resetPassword(String phone, String newPassword, DataCallback<Void> callback);

    void logout(DataCallback<Void> callback);


    /** 3. Chọn/cập nhật vai trò sau khi đăng nhập/đăng ký thành công */
    void selectRole(UserRole role, DataCallback<Void> callback);

    /** Cập nhật thông tin cá nhân */
    void updatePersonalInfo(String fullName, String dob, String gender, DataCallback<Void> callback);


    // XỬ LÝ CÔNG TY THEO VAI TRÒ
    void createCompany(String companyName, DataCallback<String> callback);
    void joinCompanyWithCode(String invitationCode, DataCallback<String> callback);

}