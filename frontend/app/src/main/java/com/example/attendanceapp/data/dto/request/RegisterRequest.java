package com.example.attendanceapp.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("phone_number")
    private String phoneNumber;
    private String password;

    public RegisterRequest(String phoneNumber, String password) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}