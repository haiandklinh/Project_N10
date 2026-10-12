package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class JoinCompanyRequest {

    @SerializedName("OTP")
    private String otp;

    @SerializedName("fullName")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    public JoinCompanyRequest(String otp, String name, String avatar) {
        this.otp = otp;
        this.name = name;
        this.avatar = avatar;
    }

    // Getters & Setters
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}