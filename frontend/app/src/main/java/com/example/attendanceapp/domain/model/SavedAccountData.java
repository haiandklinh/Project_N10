package com.example.attendanceapp.domain.model;

/** Thong tin tai khoan da luu tren may, hien thi o man LoginSavedAccountFragment */
public class SavedAccountData {
    public final String fullName;
    public final String companyName;
    public final String companyCode;
    public final String phone;

    public SavedAccountData(String fullName, String companyName, String companyCode, String phone) {
        this.fullName = fullName;
        this.companyName = companyName;
        this.companyCode = companyCode;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public String getPhone() {
        return phone;
    }
}