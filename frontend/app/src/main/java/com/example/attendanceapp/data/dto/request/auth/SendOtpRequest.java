package com.example.attendanceapp.data.dto.request.auth;

import com.google.gson.annotations.SerializedName;

public class SendOtpRequest {

    @SerializedName("phone_number")
    private String phone;

    public SendOtpRequest() {
    }

    public SendOtpRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
