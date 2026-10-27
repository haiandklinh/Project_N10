package com.example.attendanceapp.core.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.attendanceapp.R;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.ui.main.auth.AuthActivity;

public final class SystemNotificationHelper {

    public static final String CHANNEL_ID = "attendance_realtime_channel";
    private static final String CHANNEL_NAME = "Thông báo hệ thống chấm công";
    private static final String CHANNEL_DESC = "Kênh nhận thông báo realtime về đổi ca, nghỉ phép, chấm công";

    private SystemNotificationHelper() {
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESC);
                channel.enableVibration(true);
                channel.enableLights(true);
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, Notification notification) {
        if (context == null || notification == null) return;

        // Kiểm tra quyền POST_NOTIFICATIONS trên Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Người dùng chưa cấp quyền thông báo hệ thống
                return;
            }
        }

        createNotificationChannel(context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        // Intent mở ứng dụng khi nhấn vào thông báo trên thanh điều hướng/trạng thái
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent == null) {
            intent = new Intent(context, AuthActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("EXTRA_NAVIGATE_TO", "NOTIFICATIONS");
        intent.putExtra("EXTRA_NOTIFICATION_ID", notification.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int iconRes = R.drawable.ic_notifications;
        if (notification.getType() != null) {
            switch (notification.getType()) {
                case SCHEDULE:
                    iconRes = R.drawable.ic_schedule;
                    break;
                case LEAVE:
                    iconRes = R.drawable.ic_remove;
                    break;
                case ATTENDANCE:
                    iconRes = R.drawable.ic_history;
                    break;
                case PAYROLL:
                    iconRes = R.drawable.ic_payroll;
                    break;
                default:
                    iconRes = R.drawable.ic_notifications;
                    break;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconRes)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        int notificationId = (notification.getId() != null) ? notification.getId().hashCode() : (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }
}
