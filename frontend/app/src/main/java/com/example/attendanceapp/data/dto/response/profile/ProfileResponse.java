package com.example.attendanceapp.data.dto.response.profile;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("date")
    private String date;

    @SerializedName("gender")
    private String gender;

    @SerializedName("status")
    private String status;

    @Nullable
    @SerializedName("avatar_url")
    private String avatarUrl;

    public ProfileResponse() {}

    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getStatus() { return status; }
    @Nullable public String getAvatarUrl() { return avatarUrl; }
}