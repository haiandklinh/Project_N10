package com.example.attendanceapp.data.dto.request;

public class RegisterRequest {
    private String fullName;
    private String phoneNumber;
    private String password;
    private String role; // "MANAGER" hoặc "EMPLOYEE"

    public RegisterRequest(String fullName, String phoneNumber, String password, String role) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}