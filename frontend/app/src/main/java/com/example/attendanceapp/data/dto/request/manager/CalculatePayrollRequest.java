package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class CalculatePayrollRequest {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("base_salary")
    private double baseSalary;

    public CalculatePayrollRequest(String staffId, double baseSalary) {
        this.staffId = staffId;
        this.baseSalary = baseSalary;
    }

    // Getters and Setters
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }
}
