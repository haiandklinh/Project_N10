package com.example.attendanceapp.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class LogoutRequest {

    @SerializedName("refresh_token")
    private String refreshToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}