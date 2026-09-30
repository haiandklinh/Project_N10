package com.example.attendanceapp.data.dto.response.auth;
import com.google.gson.annotations.SerializedName;

public class SendOtpResponse {
    private String message;
    private String otp;

    public String getMessage() { return message; }
    public String getOtp() { return otp; }
}