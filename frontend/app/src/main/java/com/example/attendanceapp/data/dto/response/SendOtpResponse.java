package com.example.attendanceapp.data.dto.response;
import com.google.gson.annotations.SerializedName;

public class SendOtpResponse {

    @SerializedName("OTP")
    private String otp;

    public SendOtpResponse() {
    }

    public SendOtpResponse(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}