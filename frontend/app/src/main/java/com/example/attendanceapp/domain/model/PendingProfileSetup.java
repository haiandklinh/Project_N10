package com.example.attendanceapp.domain.model;

public class PendingProfileSetup {
    private UserRole role;
    private String fullName;
    private String dob;
    private String gender;

    public PendingProfileSetup() {}

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getFullName() { return fullName; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }

    public void updatePersonalInfo(String fullName, String dob, String gender) {
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
    }
}