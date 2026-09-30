package com.example.attendanceapp.data.dto.response.auth;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponse {
    @SerializedName("reset_token")
    private String resetToken;
    
    @SerializedName("is_verified")
    private boolean isVerified;

    public VerifyOtpResponse() {
    }

    public VerifyOtpResponse(String resetToken, boolean isVerified) {
        this.resetToken = resetToken;
        this.isVerified = isVerified;
    }

    public String getResetToken() {
        return resetToken;
    }

    public boolean isVerified() {
        return isVerified;
    }

}
