package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.LoginRequest;
import com.example.attendanceapp.data.dto.request.RegisterRequest;
import com.example.attendanceapp.data.dto.request.ResetPasswordRequest;
import com.example.attendanceapp.data.dto.request.VerifyOtpRequest;
import com.example.attendanceapp.data.dto.response.ApiResponse;
import com.example.attendanceapp.data.dto.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @POST("auth/send-otp")
    Call<ApiResponse<Void>> sendOtp(@Body VerifyOtpRequest request); // Hoặc DTO tương ứng

    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest request);

}