package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class CreateManagerRequest {
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("date")
    private String date;
    @SerializedName("gender")
    private String gender;
    @SerializedName("avatar")
    private String avatar;
    @SerializedName("companyName")
    private String companyName;

    public CreateManagerRequest(String fullName, String date, String gender, String avatar, String companyName) {
        this.fullName = fullName;
        this.date = date;
        this.gender = gender;
        this.avatar = avatar;
        this.companyName = companyName;
    }

    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getAvatar() { return avatar; }
    public String getCompanyName() { return companyName; }
}
