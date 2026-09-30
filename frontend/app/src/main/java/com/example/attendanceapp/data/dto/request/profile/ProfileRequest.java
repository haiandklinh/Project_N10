package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class ProfileRequest {

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("date")
    private String date; // yyyy-MM-dd

    @SerializedName("gender")
    private String gender;

    @SerializedName("avatar")
    private String avatar;

    public ProfileRequest(String fullName, String date, String gender, String avatar) {
        this.fullName = fullName;
        this.date = date;
        this.gender = gender;
        this.avatar = avatar;
    }

    // Getters & Setters
    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getAvatar() { return avatar; }
}