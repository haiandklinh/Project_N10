package com.example.attendanceapp.data.dto.request;

public class ResetPasswordRequest {
    private String phoneNumber;
    private String newPassword;
    private String otpCode; // Hoặc token xác thực sau khi verify OTP

    public ResetPasswordRequest(String phoneNumber, String newPassword, String otpCode) {
        this.phoneNumber = phoneNumber;
        this.newPassword = newPassword;
        this.otpCode = otpCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}