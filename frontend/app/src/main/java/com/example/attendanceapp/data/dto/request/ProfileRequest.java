package com.example.attendanceapp.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class ProfileRequest {
    @SerializedName("full_name")
    private String fullName;
    
    private String date;
    
    private String gender;
    
    private String avatar;

    @SerializedName("company_name")
    private String companyName; // Dùng cho Manager tạo cty

    @SerializedName("company_code")
    private String companyCode; // Dùng cho Staff nhập mã cty

    public ProfileRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
}