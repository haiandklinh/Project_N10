
package com.example.attendanceapp.domain.model;

public class EmployeeProfile {
    private final String userId;          // FK nối tới UserAccount
    private final String employeeId;      // "NV001"
    private final String fullName;
    private final String phoneNumber;
    private final String avatarUrl;
    private final String department;
    private final String position;
    private final String managerId;       // ID của Quản lý trực tiếp
    private final WorkLocation workLocation; // Embedded Location Model

    private EmployeeProfile(Builder builder) {
        this.userId = builder.userId;
        this.employeeId = builder.employeeId;
        this.fullName = builder.fullName;
        this.phoneNumber = builder.phoneNumber;
        this.avatarUrl = builder.avatarUrl;
        this.department = builder.department;
        this.position = builder.position;
        this.managerId = builder.managerId;
        this.workLocation = builder.workLocation;
    }

    public String getUserId() { return userId; }
    public String getEmployeeId() { return employeeId; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public String getManagerId() { return managerId; }
    public WorkLocation getWorkLocation() { return workLocation; }

    public static class Builder {
        private String userId;
        private String employeeId;
        private String fullName;
        private String phoneNumber;
        private String avatarUrl;
        private String department;
        private String position;
        private String managerId;
        private WorkLocation workLocation;

        public Builder setUserId(String userId) { this.userId = userId; return this; }
        public Builder setEmployeeId(String employeeId) { this.employeeId = employeeId; return this; }
        public Builder setFullName(String fullName) { this.fullName = fullName; return this; }
        public Builder setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; return this; }
        public Builder setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder setDepartment(String department) { this.department = department; return this; }
        public Builder setPosition(String position) { this.position = position; return this; }
        public Builder setManagerId(String managerId) { this.managerId = managerId; return this; }
        public Builder setWorkLocation(WorkLocation workLocation) { this.workLocation = workLocation; return this; }

        public EmployeeProfile build() {
            return new EmployeeProfile(this);
        }
    }
}