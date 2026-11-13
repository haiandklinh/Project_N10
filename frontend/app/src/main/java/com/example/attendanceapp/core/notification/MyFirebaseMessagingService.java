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

        String title = null;
        String message = null;

        // 1. Kiểm tra data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String otp = remoteMessage.getData().get("otp");
            if (otp != null && !otp.trim().isEmpty()) {
                title = "Mã xác thực OTP";
                message = "Mã của bạn là: " + otp;
            } else {
                title = remoteMessage.getData().get("title");
                message = remoteMessage.getData().get("message");
                if (message == null) {
                    message = remoteMessage.getData().get("body");
                }
            }
        }

        // 2. Kiểm tra notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                message = remoteMessage.getNotification().getBody();
            }
        }

        if (title != null || message != null) {
            showNotification(title != null ? title : "Thông báo mới", message != null ? message : "");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
    }

    public static void showStaticNotification(Context context, Notification notification) {
        if (context == null || notification == null) return;
        SystemNotificationHelper.showNotification(context, notification);
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
        SystemNotificationHelper.showNotification(this, notification);
    }
}
