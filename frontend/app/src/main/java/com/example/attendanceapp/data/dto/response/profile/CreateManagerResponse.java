package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;

public class CreateManagerResponse {
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
    @SerializedName("companyName")
    private String companyName;

    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getStatus() { return status; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCompanyName() { return companyName; }
}
