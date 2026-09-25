package com.example.attendanceapp.data.dto.response;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponse {
    @SerializedName("reset_token")
    private String resetToken;
    
    @SerializedName("is_verified")
    private boolean isVerified;
    
    @SerializedName("OTP")
    private String otp;

    public VerifyOtpResponse() {
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}