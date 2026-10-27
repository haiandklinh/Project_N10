package com.example.attendanceapp.data.dto.request.staff;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class UpdateStaffProfileRequest {
    @Nullable
    @SerializedName("full_name")
    private String fullName;

    @Nullable
    @SerializedName("gender")
    private String gender;

    @Nullable
    @SerializedName("avatar")
    private String avatar;

    @Nullable
    @SerializedName("date")
    private String birthDate;

    public UpdateStaffProfileRequest() {
    }

    public UpdateStaffProfileRequest(String fullName, String gender, String avatar, String birthDate) {
        this.fullName = fullName;
        this.gender = gender;
        this.avatar = avatar;
        this.birthDate = birthDate;
    }

    @Nullable
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Nullable
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Nullable
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}