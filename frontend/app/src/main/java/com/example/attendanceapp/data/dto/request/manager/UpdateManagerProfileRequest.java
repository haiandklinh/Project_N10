package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class UpdateManagerProfileRequest {
    @SerializedName("full_name")
    private String fullName;

    @SerializedName("gender")
    private String gender;

    @SerializedName("company")
    private String company;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("date")
    private String date;

    public UpdateManagerProfileRequest() {
    }

    public UpdateManagerProfileRequest(String fullName, String gender, String company, String avatar, String date) {
        this.fullName = fullName;
        this.gender = gender;
        this.company = company;
        this.avatar = avatar;
        this.date = date;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}