package com.example.attendanceapp.data.local.entity;
import androidx.room.Entity;
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

    public UserEntity(String name, String phone, String password, String role) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    // Getter & Setter
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
}