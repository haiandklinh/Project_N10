package com.example.attendanceapp.data.dto.response.manager;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class StaffPayrollSummaryResponse implements Serializable {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("number_of_work_shifts")
    private int numberOfWorkShifts;

    @SerializedName("total_bonus")
    private double totalBonus;

    @SerializedName("total_deduction")
    private double totalDeduction;

    @SerializedName("shifts")
    private List<ShiftItem> shifts;

    @SerializedName("bonuses")
    private List<BonusItem> bonuses;

    @SerializedName("deductions")
    private List<DeductionItem> deductions;

    public StaffPayrollSummaryResponse() {}

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public int getNumberOfWorkShifts() {
        return numberOfWorkShifts;
    }

    public void setNumberOfWorkShifts(int numberOfWorkShifts) {
        this.numberOfWorkShifts = numberOfWorkShifts;
    }

    public double getTotalBonus() {
        return totalBonus;
    }

    public void setTotalBonus(double totalBonus) {
        this.totalBonus = totalBonus;
    }

    public double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public List<ShiftItem> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftItem> shifts) {
        this.shifts = shifts;
    }

    public List<BonusItem> getBonuses() {
        return bonuses;
    }

    public void setBonuses(List<BonusItem> bonuses) {
        this.bonuses = bonuses;
    }

    public List<DeductionItem> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<DeductionItem> deductions) {
        this.deductions = deductions;
    }

    public static class ShiftItem implements Serializable {
        @SerializedName("id_attendance")
        private String attendanceId;
        @SerializedName("work_day")
        private String workDay;
        @SerializedName("shift_name")
        private String shiftName;
        @SerializedName("check_in_time")
        private String checkInTime;
        @SerializedName("check_out_time")
        private String checkOutTime;
        @SerializedName("status")
        private String status;

        public ShiftItem() {}

        public String getAttendanceId() {
            return attendanceId;
        }

        public void setAttendanceId(String attendanceId) {
            this.attendanceId = attendanceId;
        }

        public String getWorkDay() {
            return workDay;
        }

        public void setWorkDay(String workDay) {
            this.workDay = workDay;
        }

        public String getShiftName() {
            return shiftName;
        }

        public void setShiftName(String shiftName) {
            this.shiftName = shiftName;
        }

        public String getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(String checkInTime) {
            this.checkInTime = checkInTime;
        }

        public String getCheckOutTime() {
            return checkOutTime;
        }

        public void setCheckOutTime(String checkOutTime) {
            this.checkOutTime = checkOutTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class BonusItem implements Serializable {
        @SerializedName("id_bonus")
        private String bonusId;
        @SerializedName("bonus_amount")
        private double amount;
        @SerializedName("reason")
        private String reason;
        @SerializedName("note")
        private String note;

        public BonusItem() {}

        public String getBonusId() {
            return bonusId;
        }

        public void setBonusId(String bonusId) {
            this.bonusId = bonusId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
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

    public static class DeductionItem implements Serializable {
        @SerializedName("id_deduction")
        private String deductionId;
        @SerializedName("amount")
        private double amount;
        @SerializedName("deduction_type")
        private String type;
        @SerializedName("reason")
        private String reason;
        @SerializedName("created_at")
        private String createdAt;

        public DeductionItem() {}

        public String getDeductionId() {
            return deductionId;
        }

        public void setDeductionId(String deductionId) {
            this.deductionId = deductionId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
