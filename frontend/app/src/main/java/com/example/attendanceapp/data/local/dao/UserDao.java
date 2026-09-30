package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.attendanceapp.data.local.entity.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

@Dao
public interface UserDao {

    // ===== Session Management =====

    // Lấy user hiện đang đăng nhập (dùng Flowable để observe changes)
    @Query("SELECT * FROM users LIMIT 1")
    Flowable<UserEntity> getCurrentUser();

    // Lấy user hiện đang đăng nhập (dùng Maybe cho single query)
    @Query("SELECT * FROM users LIMIT 1")
    Maybe<UserEntity> getCurrentUserSingle();

    // Lưu hoặc cập nhật session user (replace nếu đã tồn tại)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdateUser(UserEntity user);

    // Xóa toàn bộ session (đăng xuất)
    @Query("DELETE FROM users")
    void clearAllUsers();

    // ===== User Operations =====

    // 1. Kiểm tra xem số điện thoại đã tồn tại trong DB chưa
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE phone = :phone)")
    boolean isPhoneExists(String phone);

    // 2. Lấy thông tin User theo SĐT
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    Maybe<UserEntity> getUserByPhone(String phone);

    // 3. Lấy thông tin User theo userId
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    Maybe<UserEntity> getUserByUserId(String userId);

    // 4. Cập nhật role của user
    @Query("UPDATE users SET role = :role WHERE phone = :phone")
    void updateRole(String phone, String role);

    // 5. Cập nhật thông tin cá nhân của user
    @Query("UPDATE users SET name = :name, dateOfBirth = :dob, gender = :gender WHERE phone = :phone")
    void updatePersonalInfo(String phone, String name, String dob, String gender);

    // 6. Cập nhật ảnh đại diện của user
    @Query("UPDATE users SET avatarUrl = :avatarUrl WHERE phone = :phone")
    void updateAvatarUrl(String phone, String avatarUrl);

    // 7. Cập nhật status của user
    @Query("UPDATE users SET status = :status WHERE phone = :phone")
    void updateStatus(String phone, String status);

    // 8. Cập nhật thông tin công ty
    @Query("UPDATE users SET companyId = :companyId, companyName = :companyName, invitationCode = :invitationCode WHERE phone = :phone")
    void updateCompanyInfo(String phone, String companyId, String companyName, String invitationCode);

    // 9. Cập nhật trạng thái firstLogin
    @Query("UPDATE users SET firstLogin = :firstLogin WHERE phone = :phone")
    void updateFirstLogin(String phone, boolean firstLogin);

    // 10. Cập nhật toàn bộ thông tin User
    @Update
    int updateUser(UserEntity user);

    // 11. Lấy danh sách tất cả người dùng
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    // 12. Xóa user cụ thể
    @Delete
    void deleteUser(UserEntity user);
}