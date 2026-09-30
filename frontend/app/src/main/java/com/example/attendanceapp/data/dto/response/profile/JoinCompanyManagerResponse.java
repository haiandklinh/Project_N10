package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;
import androidx.annotation.Nullable;

public class JoinCompanyManagerResponse {
    @SerializedName("manager_id")
    private String managerId;

    @SerializedName("manager_name")
    private String managerName;

    @SerializedName("company_name")
    private String companyName;

    @Nullable
    @SerializedName("avatar_url")
    private String avatarUrl;

    public JoinCompanyManagerResponse() {
    }

    public JoinCompanyManagerResponse(String managerId, String managerName, String companyName, String avatarUrl) {
        this.managerId = managerId;
        this.managerName = managerName;
        this.companyName = companyName;
        this.avatarUrl = avatarUrl;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
