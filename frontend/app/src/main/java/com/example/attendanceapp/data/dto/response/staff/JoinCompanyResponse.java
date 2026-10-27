package com.example.attendanceapp.data.dto.response.staff;

import com.google.gson.annotations.SerializedName;

public class JoinCompanyResponse {
    @SerializedName("company")
    private String company;

    @SerializedName("OTP")
    private String otp;

    public JoinCompanyResponse() {
    }

    public JoinCompanyResponse(String company, String otp) {
        this.company = company;
        this.otp = otp;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}