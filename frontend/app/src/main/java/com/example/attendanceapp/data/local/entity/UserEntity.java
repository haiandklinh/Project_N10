package com.example.attendanceapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"phone"}, unique = true)} // Đảm bảo SĐT không bị trùng
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // Basic user info
    private String userId;      // User ID từ server
    private String name;
    private String phone;
    private String role;
    private String status;      // UserStatus: APPROVED, PENDING_APPROVAL, NEEDS_ROLE_SELECTION
    private String dateOfBirth;
    private String gender;
    private String avatarUrl;

    // Company info
    private String companyId;
    private String companyName;
    private String invitationCode;

    // Session state
    private boolean firstLogin;
    private boolean active;

    // Constructor Room sử dụng chính
    public UserEntity(String userId, String name, String phone, String role, String status) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.firstLogin = true; // Trả về mặc định true cho người dùng mới
        this.active = true;
    }

    // Constructor đầy đủ tham số (dùng @Ignore để Room không bị xung đột)
    @Ignore
    public UserEntity(String userId, String name, String phone, String role, String status,
                      String dateOfBirth, String gender, String avatarUrl,
                      String companyId, String companyName, String invitationCode,
                      boolean firstLogin, boolean active) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
        this.companyId = companyId;
        this.companyName = companyName;
        this.invitationCode = invitationCode;
        this.firstLogin = firstLogin;
        this.active = active;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getInvitationCode() { return invitationCode; }
    public void setInvitationCode(String invitationCode) { this.invitationCode = invitationCode; }

    public boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(boolean firstLogin) { this.firstLogin = firstLogin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}