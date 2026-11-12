package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class CreateBonusRequest {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("bonus_amount")
    private double bonusAmount;

    @SerializedName("reason")
    private String reason;

    @SerializedName("note")
    private String note;

    public CreateBonusRequest(String staffId, double bonusAmount, String reason, String note) {
        this.staffId = staffId;
        this.bonusAmount = bonusAmount;
        this.reason = reason;
        this.note = note;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
