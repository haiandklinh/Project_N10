package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;

public class CreateEmployeeResponse {
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("status")
    private String status;
    @SerializedName("date")
    private String date;
    @SerializedName("gender")
    private String gender;
    @SerializedName("avatar")
    private String avatarUrl;

    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getStatus() { return status; }
    public String getAvatarUrl() { return avatarUrl; }
}
