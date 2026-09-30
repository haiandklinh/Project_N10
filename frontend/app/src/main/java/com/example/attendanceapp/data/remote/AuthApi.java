package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.auth.CheckPhoneRequest;
import com.example.attendanceapp.data.dto.request.auth.LoginRequest;
import com.example.attendanceapp.data.dto.request.auth.RefreshTokenRequest; // Bổ sung DTO này
import com.example.attendanceapp.data.dto.request.auth.RegisterRequest;
import com.example.attendanceapp.data.dto.request.auth.ResetPasswordRequest;
import com.example.attendanceapp.data.dto.request.auth.SendOtpRequest;
import com.example.attendanceapp.data.dto.request.auth.VerifyOtpRequest;
import com.example.attendanceapp.data.dto.response.auth.AuthResponse;
import com.example.attendanceapp.data.dto.response.auth.CheckPhoneResponse;
import com.example.attendanceapp.data.dto.response.auth.RefreshTokenResponse;
import com.example.attendanceapp.data.dto.response.auth.SendOtpResponse;
import com.example.attendanceapp.data.dto.response.auth.VerifyOtpResponse;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
public interface AuthApi {

    @POST("auth/check-phone")
    Single<CheckPhoneResponse> checkPhone(@Body CheckPhoneRequest request);

    @POST("auth/register")
    Completable register(@Body RegisterRequest request);

    @POST("auth/login")
    Single<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/logout")
    Completable logout(@Body RefreshTokenRequest request);

    // OTP đăng ký
    @POST("auth/register/send-otp")
    Single<SendOtpResponse> sendRegisterOtp(@Body SendOtpRequest request);

    @POST("auth/register/verify-otp")
    Single<VerifyOtpResponse> verifyRegisterOtp(@Body VerifyOtpRequest request);

    // Quên mật khẩu
    @POST("auth/forgot-password/send-otp")
    Single<SendOtpResponse> sendForgotPasswordOtp(@Body SendOtpRequest request);

    @POST("auth/forgot-password/verify-otp")
    Single<VerifyOtpResponse> verifyForgotPasswordOtp(@Body VerifyOtpRequest request);

    @PATCH("auth/forgot-password/reset")
    Completable resetPassword(@Body ResetPasswordRequest request);

    // Chỉ dùng trong Authenticator, giữ Call để gọi đồng bộ
    @POST("auth/refresh-token")
    Call<RefreshTokenResponse> refreshToken(@Body RefreshTokenRequest request);
}