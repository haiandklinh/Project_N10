package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;

public class GetOtpResponse {
    private String otp;
    @SerializedName("expires_in")
    private int expiresIn;
    private String message;

    public String getOtp() { return otp; }
    public int getExpiresIn() { return expiresIn; }
    public String getMessage() { return message; }
}