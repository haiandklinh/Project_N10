package com.example.attendanceapp.domain.model;

public class Salary {
    private String month;
    private long basicSalary; // Full-time: Lương cứng hàng tháng
    private long allowance;   // Phụ cấp
    private long bonus;       // Thưởng
    private long deductions;  // Khấu trừ / Phạt
    private long totalSalary; // Tổng thực nhận

    // Full-time specific
    private int totalWorkingDays;
    private int actualWorkingDays;

    // Part-time specific
    private double totalHours;
    private long hourlyRate;

    private EmploymentType employmentType; // Refactor: boolean isPartTime -> EmploymentType
    private PaymentStatus paymentStatus;   // Thêm trạng thái thanh toán

    // Constructor cho Full-time
    public Salary(String month, long basicSalary, long allowance, long bonus, long deductions,
                  int totalWorkingDays, int actualWorkingDays, PaymentStatus paymentStatus) {
        this.month = month;
        this.basicSalary = basicSalary;
        this.allowance = allowance;
        this.bonus = bonus;
        this.deductions = deductions;
        this.totalWorkingDays = totalWorkingDays;
        this.actualWorkingDays = actualWorkingDays;
        this.employmentType = EmploymentType.FULL_TIME;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.UNPAID;
        calculateTotal();
    }

    // Constructor cho Part-time
    public Salary(String month, long hourlyRate, double totalHours, long allowance,
                  long bonus, long deductions, PaymentStatus paymentStatus) {
        this.month = month;
        this.hourlyRate = hourlyRate;
        this.totalHours = totalHours;
        this.allowance = allowance;
        this.bonus = bonus;
        this.deductions = deductions;
        this.employmentType = EmploymentType.PART_TIME;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.UNPAID;
        calculateTotal();
    }

    private void calculateTotal() {
        if (employmentType == EmploymentType.PART_TIME) {
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
    public EmploymentType getEmploymentType() { return employmentType; }
    public boolean isPartTime() { return employmentType == EmploymentType.PART_TIME; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}