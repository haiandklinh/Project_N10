package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.attendanceapp.data.local.entity.UserEntity;

@Dao
public interface UserDao {

    // Kiểm tra xem số điện thoại đã tồn tại trong DB chưa
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE phone = :phone)")
    boolean isPhoneExists(String phone);

    // Thêm user mới
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(UserEntity user);

    // Lấy thông tin user bằng SĐT và Password khi đăng nhập
    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    UserEntity login(String phone, String password);

    // Cập nhật role của user
    @Query("UPDATE users SET role = :role WHERE phone = :phone")
    void updateRole(String phone, String role);

    // Cập nhật password của user
    @Query("UPDATE users SET password = :newPassword WHERE phone = :phone")
    void updatePassword(String phone, String newPassword);
}