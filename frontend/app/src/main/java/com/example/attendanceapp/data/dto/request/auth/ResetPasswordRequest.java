package com.example.attendanceapp.data.dto.request.auth;

import com.google.gson.annotations.SerializedName;

//X
public class  ResetPasswordRequest {
    @SerializedName("reset_token")
    private String resetToken;
    
    @SerializedName("password")
    private String newPassword;

    public ResetPasswordRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}