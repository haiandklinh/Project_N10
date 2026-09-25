package com.example.attendanceapp.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class SendOtpRequest {

    @SerializedName("phone")
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