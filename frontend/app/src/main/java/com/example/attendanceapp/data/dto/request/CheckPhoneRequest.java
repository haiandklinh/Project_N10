package com.example.attendanceapp.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class CheckPhoneRequest {

    @SerializedName("phone_number")
    private String phone;

    public CheckPhoneRequest() {
    }

    public CheckPhoneRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}