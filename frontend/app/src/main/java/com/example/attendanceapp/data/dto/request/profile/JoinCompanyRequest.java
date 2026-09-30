package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class JoinCompanyRequest {
    @SerializedName("OTP")
    private String otp;

    private String name;
    private String avatar;

    public JoinCompanyRequest(String otp, String name, String avatar) {
        this.otp = otp;
        this.name = name;
        this.avatar = avatar;
    }

    public String getOtp() { return otp; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }
}