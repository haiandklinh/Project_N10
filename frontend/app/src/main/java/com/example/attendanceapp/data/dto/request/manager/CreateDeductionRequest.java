package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class CreateDeductionRequest {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("deduction_type")
    private String deductionType;

    @SerializedName("reason")
    private String reason;

    public CreateDeductionRequest(String staffId, double amount, String deductionType, String reason) {
        this.staffId = staffId;
        this.amount = amount;
        this.deductionType = deductionType;
        this.reason = reason;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDeductionType() {
        return deductionType;
    }

    public void setDeductionType(String deductionType) {
        this.deductionType = deductionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
