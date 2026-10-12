package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class UpdateEmployeeRequest {
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("gender")
    private String gender;
    @SerializedName("avatar")
    private String avatar;

    @SerializedName("date")
    private String date;

    public UpdateEmployeeRequest(String fullName, String gender, String avatar, String date) {
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
