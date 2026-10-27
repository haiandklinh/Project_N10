package com.example.attendanceapp.data.dto.request.staff;

import com.google.gson.annotations.SerializedName;

public class CreateStaffProfileRequest {
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("date")
    private String date;
    @SerializedName("gender")
    private String gender;
    @SerializedName("avatar")
    private String avatar;

    public CreateStaffProfileRequest(String fullName, String date, String gender, String avatar) {
        this.fullName = fullName;
        this.date = date;
        this.gender = gender;
        this.avatar = avatar;
    }

    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getAvatar() { return avatar; }
}
