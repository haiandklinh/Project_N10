package com.example.attendanceapp.data.dto.request.auth;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("phone_number")
    private String phoneNumber;
    
    @SerializedName("password")
    private String newPassword;

    public ResetPasswordRequest(String phoneNumber, String newPassword) {
        this.phoneNumber = phoneNumber;
        this.newPassword = newPassword;
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
}
