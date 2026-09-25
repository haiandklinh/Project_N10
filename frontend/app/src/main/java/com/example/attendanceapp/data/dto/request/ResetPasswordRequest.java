package com.example.attendanceapp.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    private String phone;
    
    @SerializedName("password")
    private String newPassword;

    public ResetPasswordRequest(String phone, String newPassword) {
        this.phone = phone;
        this.newPassword = newPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}