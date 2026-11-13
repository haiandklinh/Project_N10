package com.example.attendanceapp.core.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.data.mapper.StaffScheduleMapper;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import java.time.LocalDate;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * BroadcastReceiver đồng bộ thông báo và ca làm việc định kỳ mỗi 15 phút
 * bằng AlarmManager (setAndAllowWhileIdle), đảm bảo hoạt động ngay cả khi điện thoại tắt màn hình,
 * ở chế độ tiết kiệm pin (Doze Mode) hoặc ứng dụng đã bị đóng.
 */
@AndroidEntryPoint
public class NotificationSyncReceiver extends BroadcastReceiver {

    private static final String TAG = "NotifSyncReceiver";
    public static final String ACTION_SYNC_NOTIFICATIONS = "com.example.attendanceapp.ACTION_SYNC_NOTIFICATIONS";
    private static final int REQUEST_CODE = 9902;
    private static final long INTERVAL_MS = 15 * 60 * 1000L; // 15 phút

    @Inject
    TokenManager tokenManager;

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    StaffRepository staffRepository;

    @Inject
    AppStateRepository appStateRepository;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    @Inject
    WebSocketNotificationManager webSocketManager;

    public static void schedulePeriodicSync(Context context) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationSyncReceiver.class);
        intent.setAction(ACTION_SYNC_NOTIFICATIONS);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MS;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
            }
            Log.d(TAG, "Scheduled periodic sync in " + (INTERVAL_MS / 1000) + "s");
        } catch (Exception e) {
            Log.w(TAG, "Failed to schedule periodic sync", e);
        }
    }

    public static void cancelPeriodicSync(Context context) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationSyncReceiver.class);
        intent.setAction(ACTION_SYNC_NOTIFICATIONS);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Cancelled periodic sync");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || ACTION_SYNC_NOTIFICATIONS.equals(action)) {
            // Lên lịch chu kỳ 15 phút tiếp theo
            schedulePeriodicSync(context);

            if (tokenManager == null || !tokenManager.hasToken()) {
                Log.d(TAG, "Not logged in, skipping background sync");
                return;
            }

            // 1. Đảm bảo WebSocket kết nối
            if (webSocketManager != null) {
                webSocketManager.connect();
            }

            // 3. Đồng bộ thông báo mới từ backend REST API
            if (notificationRepository != null) {
                notificationRepository.refreshNotifications()
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> Log.d(TAG, "Sync notifications completed"),
                                throwable -> Log.w(TAG, "Sync notifications error: " + throwable.getMessage()));
            }

            // 4. Kiểm tra ca hôm nay nếu là nhân viên để lên lịch chuông nhắc nhở chấm công
            UserRole role = appStateRepository != null ? appStateRepository.getUserRole() : null;
            if (role == UserRole.EMPLOYEE && staffRepository != null) {
                staffRepository.getWorkSchedules()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                resp -> {
                                    if (resp != null && resp.getData() != null) {
                                        Shift todayShift = StaffScheduleMapper.pickTodayShift(
                                                StaffScheduleMapper.toShifts(resp.getData(), sharedPreferenceApp),
                                                LocalDate.now()
                                        );
                                        if (todayShift != null) {
                                            ShiftAlarmScheduler.scheduleShiftAlarms(context, todayShift, sharedPreferenceApp);
                                        }
                                    }
                                },
                                throwable -> Log.w(TAG, "Sync work schedules error: " + throwable.getMessage())
                        );
            }
        }
    }
}
