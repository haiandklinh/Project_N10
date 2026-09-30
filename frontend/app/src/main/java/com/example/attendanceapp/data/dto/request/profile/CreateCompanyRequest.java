package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class CreateCompanyRequest {

    @SerializedName("company_name")
    private String companyName;

    public CreateCompanyRequest() {
    }

    public CreateCompanyRequest(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
