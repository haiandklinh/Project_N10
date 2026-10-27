package com.example.attendanceapp.data.dto.response.staff;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class StaffProfileResponse {
    @SerializedName(value = "fullName", alternate = {"full_name", "fullname", "name"})
    private String fullName;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "date", alternate = {"birthDate", "birth_date", "date_of_birth"})
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    @Nullable
    @SerializedName(value = "avatar", alternate = {"avatar_url", "avatarUrl", "image"})
    private String avatar;

    public StaffProfileResponse() {
    }

    public StaffProfileResponse(String fullName, String status, String birthDate, String gender, String avatar) {
        this.fullName = fullName;
        this.status = status;
        this.birthDate = birthDate;
        this.gender = gender;
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

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
}