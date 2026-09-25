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

    private String name;
    private String phone;
    private String password;
    private String role;
    private String dateOfBirth; // Đã đổi tên field/getter tương thích AuthRepositoryImpl
    private String gender;
    private String avatarUrl;   // Thêm trường ảnh đại diện

    // Constructor Room sử dụng chính
    public UserEntity(String name, String phone, String password, String role) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    // Constructor đầy đủ tham số (dùng @Ignore để Room không bị xung đột)
    @Ignore
    public UserEntity(String name, String phone, String password, String role, String dateOfBirth, String gender, String avatarUrl) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}