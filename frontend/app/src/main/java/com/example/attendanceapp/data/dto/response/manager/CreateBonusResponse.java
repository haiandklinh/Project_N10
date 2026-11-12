package com.example.attendanceapp.data.dto.response.manager;

import com.google.gson.annotations.SerializedName;

public class CreateBonusResponse {
    @SerializedName("id_bonus")
    private String bonusId;

    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("name_staff")
    private String staffName;

    @SerializedName("bonus_amount")
    private double bonusAmount;

    @SerializedName("reason")
    private String reason;

    @SerializedName("note")
    private String note;

    @SerializedName("status")
    private String status;

    public String getBonusId() {
        return bonusId;
    }

    public void setBonusId(String bonusId) {
        this.bonusId = bonusId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
