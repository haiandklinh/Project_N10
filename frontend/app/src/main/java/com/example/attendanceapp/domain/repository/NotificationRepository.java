package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.Notification;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface NotificationRepository {

    Observable<List<Notification>> observeNotifications();

    Observable<Integer> observeUnreadCount();

    Completable refreshNotifications();

    Completable markAsRead(String id);

    Completable markAllAsRead();

    void postNotification(Notification notification);

    void notifyShiftChangeRequest(String employeeName, String workDay, String reason);

    void notifyLeaveRequest(String employeeName, String workDay, String reason);

    void notifyAttendanceAdjustment(String employeeName, String workday, String reason);

    void notifyApprovalResult(String requestType, boolean approved, String reason);

    Completable deleteNotificationsByStaffId(String staffId);
}
