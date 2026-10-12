package com.example.attendanceapp.core.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.attendanceapp.R;
import com.example.attendanceapp.ui.main.auth.AuthActivity;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.time.LocalDateTime;
import java.util.UUID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "otp_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Kiểm tra nếu tin nhắn chứa data payload (backend gửi OTP dạng JSON trong data)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            // Giả sử backend gửi key là "otp"
            String otp = remoteMessage.getData().get("otp");
            if (otp != null) {
                showNotification("Mã xác thực OTP", "Mã của bạn là: " + otp);
            }
        }

        // Kiểm tra nếu tin nhắn chứa notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
    }

    public static void showStaticNotification(Context context, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo hệ thống",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        int iconRes = R.drawable.ic_notifications;
        if (notification.getType() == NotificationType.AUTH) {
            iconRes = R.drawable.ic_lock; // Hoặc ic_key tùy bạn có
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconRes)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void showNotification(String title, String message) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                title,
                message,
                LocalDateTime.now(),
                NotificationType.SYSTEM,
                false
        );
        showStaticNotification(this, notification);
    }
}
