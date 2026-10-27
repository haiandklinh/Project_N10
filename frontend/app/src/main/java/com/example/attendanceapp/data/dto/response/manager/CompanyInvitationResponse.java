package com.example.attendanceapp.data.dto.response.manager;

import com.google.gson.annotations.SerializedName;

public class CompanyInvitationResponse {
    @SerializedName(value = "OTP", alternate = {"otp", "invitation_code", "code"})
    private String otp;

    @SerializedName(value = "expires_in", alternate = {"expiresIn"})
    private String expiresIn;

    public CompanyInvitationResponse() {
    }

    public CompanyInvitationResponse(String otp, String expiresIn) {
        this.otp = otp;
        this.expiresIn = expiresIn;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
}