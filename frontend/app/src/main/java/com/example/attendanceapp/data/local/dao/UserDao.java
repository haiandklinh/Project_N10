package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.attendanceapp.data.local.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    // 1. Kiểm tra xem số điện thoại đã tồn tại trong DB chưa
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE phone = :phone)")
    boolean isPhoneExists(String phone);

    // 2. Thêm user mới
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(UserEntity user);

    // 3. Lấy thông tin user bằng SĐT và Password khi đăng nhập
    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    UserEntity login(String phone, String password);

    // 4. Lấy thông tin User theo SĐT
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    UserEntity getUserByPhone(String phone);

    // 5. Lấy thông tin User theo ID
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(int userId);

    // 6. Cập nhật role của user
    @Query("UPDATE users SET role = :role WHERE phone = :phone")
    void updateRole(String phone, String role);

    // 7. Cập nhật thông tin cá nhân của user (Sửa dob -> dateOfBirth)
    @Query("UPDATE users SET name = :name, dateOfBirth = :dob, gender = :gender WHERE phone = :phone")
    void updatePersonalInfo(String phone, String name, String dob, String gender);

    // 8. Cập nhật ảnh đại diện của user
    @Query("UPDATE users SET avatarUrl = :avatarUrl WHERE phone = :phone")
    void updateAvatarUrl(String phone, String avatarUrl);

    // 9. Cập nhật password của user
    @Query("UPDATE users SET password = :newPassword WHERE phone = :phone")
    void updatePassword(String phone, String newPassword);

    // 10. Cập nhật toàn bộ thông tin User
    @Update
    int updateUser(UserEntity user);

    // 11. Lấy danh sách tất cả người dùng
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    // 12. Xóa user
    @Delete
    void deleteUser(UserEntity user);
}