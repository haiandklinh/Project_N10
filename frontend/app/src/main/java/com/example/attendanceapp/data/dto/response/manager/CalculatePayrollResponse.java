package com.example.attendanceapp.data.dto.response.manager;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CalculatePayrollResponse implements Serializable {
    @SerializedName("id_payroll")
    private String payrollId;

    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("name_staff")
    private String staffName;

    @SerializedName("base_salary")
    private double baseSalary;

    @SerializedName("number_of_work_shifts")
    private int numberOfWorkShifts;

    @SerializedName("total_salary")
    private double totalSalary;

    @SerializedName("status")
    private String status;

    @SerializedName("time_create")
    private String timeCreate;

    public CalculatePayrollResponse() {}

    public String getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(String payrollId) {
        this.payrollId = payrollId;
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

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public int getNumberOfWorkShifts() {
        return numberOfWorkShifts;
    }

    public void setNumberOfWorkShifts(int numberOfWorkShifts) {
        this.numberOfWorkShifts = numberOfWorkShifts;
    }

    public double getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(double totalSalary) {
        this.totalSalary = totalSalary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(String timeCreate) {
        this.timeCreate = timeCreate;
    }
}
