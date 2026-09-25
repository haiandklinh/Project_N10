package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.CheckPhoneRequest;
import com.example.attendanceapp.data.dto.request.LoginRequest;
import com.example.attendanceapp.data.dto.request.RefreshTokenRequest; // Bổ sung DTO này
import com.example.attendanceapp.data.dto.request.RegisterRequest;
import com.example.attendanceapp.data.dto.request.ResetPasswordRequest;
import com.example.attendanceapp.data.dto.request.SendOtpRequest;
import com.example.attendanceapp.data.dto.request.VerifyOtpRequest;
import com.example.attendanceapp.data.dto.request.ProfileRequest;
import com.example.attendanceapp.data.dto.response.AuthResponse;
import com.example.attendanceapp.data.dto.response.CheckPhoneResponse;
import com.example.attendanceapp.data.dto.response.MessageResponse;
import com.example.attendanceapp.data.dto.response.ProfileResponse;
import com.example.attendanceapp.data.dto.response.RefreshTokenResponse;
import com.example.attendanceapp.data.dto.response.SendOtpResponse;
import com.example.attendanceapp.data.dto.response.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ApiService {

    // Giả định Base URL Retrofit của bạn đã bao gồm tiền tố domain (ví dụ: https://api.example.com/auth/)
    // Nếu Base URL chưa có /auth/, hãy thêm "auth/" vào trước từng endpoint bên dưới.

    @POST("check-phone")
    Call<CheckPhoneResponse> checkPhone(@Body CheckPhoneRequest request);

    @POST("login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<MessageResponse> register(@Body RegisterRequest request);

    // Luồng Đăng ký (Register)
    @POST("register/send-otp")
    Call<SendOtpResponse> sendRegisterOtp(@Body SendOtpRequest request);

    @POST("register/verify-otp")
    Call<VerifyOtpResponse> verifyRegisterOtp(@Body VerifyOtpRequest request);

    // Luồng Quên mật khẩu (Forgot Password)
    @POST("forgot-password/send-otp")
    Call<SendOtpResponse> sendForgotPasswordOtp(@Body SendOtpRequest request);

    @POST("forgot-password/verify-otp")
    Call<VerifyOtpResponse> verifyForgotPasswordOtp(@Body VerifyOtpRequest request);

    @PATCH("forgot-password/reset")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

    // Hồ sơ cá nhân (Profile)
    @POST("profile")
    Call<ProfileResponse> createProfile(@Body ProfileRequest request);

    // Đổi Token mới khi Access Token hết hạn
    @POST("refresh-token")
    Call<RefreshTokenResponse> refreshToken(@Body RefreshTokenRequest request);

    // Đăng xuất (Yêu cầu truyền RefreshTokenRequest)
    @PATCH("logout")
    Call<MessageResponse> logout(@Body RefreshTokenRequest request);
}