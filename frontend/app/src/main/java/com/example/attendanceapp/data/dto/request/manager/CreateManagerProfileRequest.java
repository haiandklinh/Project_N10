package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class CreateManagerProfileRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("company")
    private String company;

    @SerializedName("date")
    private String date;

    @SerializedName("gender")
    private String gender;

    @SerializedName("avatar")
    private String avatar;

    public CreateManagerProfileRequest() {
    }

    public CreateManagerProfileRequest(String fullName, String company, String date, String gender, String avatar) {
        this.fullName = fullName;
        this.company = company;
        this.date = date;
        this.gender = gender;
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}