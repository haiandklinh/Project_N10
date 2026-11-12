package com.example.attendanceapp.data.dto.response.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class StaffPayrollResponse implements Serializable {
    @SerializedName("id_payroll")
    private String payrollId;

    @SerializedName("id_manager")
    private String managerId;

    @SerializedName("name_company")
    private String companyName;

    @SerializedName("name_manager")
    private String managerName;

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

    public StaffPayrollResponse() {}

    public String getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(String payrollId) {
        this.payrollId = payrollId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
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
