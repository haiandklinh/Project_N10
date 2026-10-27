package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.attendanceapp.data.local.entity.NotificationEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    Flowable<List<NotificationEntity>> observeAllNotifications();

    @Query("SELECT * FROM notifications WHERE role = :role OR role IS NULL OR role = '' ORDER BY created_at DESC")
    Flowable<List<NotificationEntity>> observeNotificationsByRole(String role);

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    Flowable<Integer> observeUnreadCount();

    @Query("SELECT COUNT(*) FROM notifications WHERE (role = :role OR role IS NULL OR role = '') AND is_read = 0")
    Flowable<Integer> observeUnreadCountByRole(String role);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<NotificationEntity> notifications);

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    Completable markAsRead(String id);

    @Query("UPDATE notifications SET is_read = 1")
    Completable markAllAsRead();

    @Query("UPDATE notifications SET is_read = 1 WHERE role = :role OR role IS NULL OR role = ''")
    Completable markAllAsReadByRole(String role);

    @Query("DELETE FROM notifications")
    Completable clearAll();

    @Query("DELETE FROM notifications WHERE role = :role")
    Completable clearByRole(String role);

    @Query("DELETE FROM notifications WHERE id = :staffId OR id LIKE '%' || :staffId || '%'")
    Completable deleteByStaffId(String staffId);
}
