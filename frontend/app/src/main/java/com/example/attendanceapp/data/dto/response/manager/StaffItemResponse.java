package com.example.attendanceapp.data.dto.response.manager;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class StaffItemResponse {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("name_staff")
    private String nameStaff;

    @Nullable
    @SerializedName("position")
    private String position;

    @Nullable
    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @Nullable
    @SerializedName("gender")
    private String gender;

    @Nullable
    @SerializedName("status")
    private String status;

    @Nullable
    @SerializedName("avatar")
    private String avatar;

    public StaffItemResponse() {
    }

    public StaffItemResponse(String staffId, String nameStaff, String position, String dateOfBirth,
                             String gender, String status, String avatar) {
        this.staffId = staffId;
        this.nameStaff = nameStaff;
        this.position = position;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = status;
        this.avatar = avatar;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getNameStaff() {
        return nameStaff;
    }

    public void setNameStaff(String nameStaff) {
        this.nameStaff = nameStaff;
    }

    @Nullable
    public String getPosition() {
        return position;
    }

    public void setPosition(@Nullable String position) {
        this.position = position;
    }

    @Nullable
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@Nullable String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Nullable
    public String getGender() {
        return gender;
    }

    public void setGender(@Nullable String gender) {
        this.gender = gender;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@Nullable String avatar) {
        this.avatar = avatar;
    }
}
