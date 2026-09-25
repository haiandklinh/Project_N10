package com.example.attendanceapp.data.dto.response;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("full_name")
    private String fullName;
    
    private String date;
    
    private String gender;
    
    private String avatar;
    
    private String status;
    
    @SerializedName("user_id")
    private String userId;

    @SerializedName("role")
    private String role;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("company_id")
    private String companyId;

    public ProfileResponse() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
}