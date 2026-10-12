package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;
import androidx.annotation.Nullable;

public class JoinCompanyPendingResponse {

    @Nullable
    @SerializedName("request_id")
    private String requestId;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("otp") // Hoặc "invitation_code" tùy theo tên field Backend trả về
    private String otp;

    @Nullable
    @SerializedName("status")
    private String status; // "PENDING" | "APPROVED" | "REJECTED"

    public JoinCompanyPendingResponse() {
    }

    public JoinCompanyPendingResponse(@Nullable String requestId, String companyName, String otp, @Nullable String status) {
        this.requestId = requestId;
        this.companyName = companyName;
        this.otp = otp;
        this.status = status;
    }

    @Nullable
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(@Nullable String requestId) {
        this.requestId = requestId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
    }
}