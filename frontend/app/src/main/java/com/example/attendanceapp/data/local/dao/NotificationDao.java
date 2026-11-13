package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.attendanceapp.data.local.entity.NotificationEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    Flowable<List<NotificationEntity>> observeAllNotifications();

    @Query("SELECT * FROM notifications WHERE role = :role ORDER BY created_at DESC")
    Flowable<List<NotificationEntity>> observeNotificationsByRole(String role);

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    Flowable<Integer> observeUnreadCount();

    @Query("SELECT COUNT(*) FROM notifications WHERE role = :role AND is_read = 0")
    Flowable<Integer> observeUnreadCountByRole(String role);

    @Query("SELECT id FROM notifications WHERE role = :role AND is_read = 1")
    Single<List<String>> getReadNotificationIdsByRole(String role);

    @Query("SELECT id FROM notifications WHERE role = :role")
    Single<List<String>> getAllNotificationIdsByRole(String role);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<NotificationEntity> notifications);

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    Completable markAsRead(String id);

    @Query("UPDATE notifications SET is_read = 1")
    Completable markAllAsRead();

    @Query("UPDATE notifications SET is_read = 1 WHERE role = :role")
    Completable markAllAsReadByRole(String role);

    @Query("DELETE FROM notifications WHERE role IS NULL OR role = ''")
    Completable deleteUnscopedNotifications();

    @Query("DELETE FROM notifications WHERE role = 'MANAGER' AND (title = 'Lịch làm việc mới' OR title = 'Cập nhật lịch làm việc' OR title LIKE 'Đã duyệt:%' OR title LIKE 'Từ chối:%' OR title LIKE 'Kết quả %')")
    Completable deleteCrossRoleNotificationsForManager();

    @Query("DELETE FROM notifications WHERE role = 'EMPLOYEE' AND title IN ('Yêu cầu gia nhập mới', 'Yêu cầu tham gia công ty', 'Yêu cầu đổi ca mới', 'Yêu cầu đổi ca làm việc', 'Đơn xin nghỉ phép mới', 'Yêu cầu điều chỉnh chấm công')")
    Completable deleteCrossRoleNotificationsForEmployee();

    @Query("DELETE FROM notifications")
    Completable clearAll();

    @Query("DELETE FROM notifications WHERE role = :role")
    Completable clearByRole(String role);

    @Query("DELETE FROM notifications WHERE id = :staffId OR id LIKE '%' || :staffId || '%'")
    Completable deleteByStaffId(String staffId);
}
