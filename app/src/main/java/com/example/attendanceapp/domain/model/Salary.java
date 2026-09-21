package com.example.attendanceapp.domain.model;

public class Salary {
    private String month;
    private long basicSalary; // For Full-time: Monthly salary; For Part-time: target or base if any
    private long allowance;
    private long bonus;
    private long deductions;
    private long totalSalary;
    
    // Full-time specific
    private int totalWorkingDays;
    private int actualWorkingDays;
    
    // Part-time specific
    private double totalHours;
    private long hourlyRate;
    
    private boolean isPartTime;

    // Constructor for Full-time
    public Salary(String month, long basicSalary, long allowance, long bonus, long deductions, int totalWorkingDays, int actualWorkingDays) {
        this.month = month;
        this.basicSalary = basicSalary;
        this.allowance = allowance;
        this.bonus = bonus;
        this.deductions = deductions;
        this.totalWorkingDays = totalWorkingDays;
        this.actualWorkingDays = actualWorkingDays;
        this.isPartTime = false;
        calculateTotal();
    }

    // Constructor for Part-time
    public Salary(String month, long hourlyRate, double totalHours, long allowance, long bonus, long deductions) {
        this.month = month;
        this.hourlyRate = hourlyRate;
        this.totalHours = totalHours;
        this.allowance = allowance;
        this.bonus = bonus;
        this.deductions = deductions;
        this.isPartTime = true;
        calculateTotal();
    }

    private void calculateTotal() {
        if (isPartTime) {
            this.totalSalary = (long) (hourlyRate * totalHours) + allowance + bonus - deductions;
        } else {
            if (totalWorkingDays > 0) {
                long workSalary = (basicSalary / totalWorkingDays) * actualWorkingDays;
                this.totalSalary = workSalary + allowance + bonus - deductions;
            } else {
                this.totalSalary = allowance + bonus - deductions;
            }
        }
    }

    // Getters and Setters
    public String getMonth() { return month; }
    public long getBasicSalary() { return basicSalary; }
    public long getAllowance() { return allowance; }
    public long getBonus() { return bonus; }
    public long getDeductions() { return deductions; }
    public long getTotalSalary() { return totalSalary; }
    public int getTotalWorkingDays() { return totalWorkingDays; }
    public int getActualWorkingDays() { return actualWorkingDays; }
    public double getTotalHours() { return totalHours; }
    public long getHourlyRate() { return hourlyRate; }
    public boolean isPartTime() { return isPartTime; }
}
