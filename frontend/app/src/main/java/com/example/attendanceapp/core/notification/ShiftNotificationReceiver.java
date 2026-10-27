package com.example.attendanceapp.core.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.ui.main.auth.AuthActivity;

public class ShiftNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "ShiftNotification";

    public static final String ACTION_SHIFT_NOTIFICATION = "com.example.attendanceapp.ACTION_SHIFT_NOTIFICATION";

    public static final String EXTRA_SCHEDULE_ID = "extra_schedule_id";
    public static final String EXTRA_WORKDAY = "extra_workday";
    public static final String EXTRA_SHIFT_NAME = "extra_shift_name";
    public static final String EXTRA_TRIGGER_TYPE = "extra_trigger_type";

    public static final int TRIGGER_ALLOWED_CHECKIN = 1;
    public static final int TRIGGER_REMINDER_15 = 2;
    public static final int TRIGGER_REMINDER_30 = 3;

    private static final String CHANNEL_ID = "shift_reminder_channel";
    private static final String CHANNEL_NAME = "Nhắc nhở ca làm việc";
    private static final String CHANNEL_DESC = "Thông báo giờ chấm công và nhắc nhở vào ca làm việc";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "BOOT_COMPLETED received");
            return;
        }

        String scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID);
        String workday = intent.getStringExtra(EXTRA_WORKDAY);
        String shiftName = intent.getStringExtra(EXTRA_SHIFT_NAME);
        int triggerType = intent.getIntExtra(EXTRA_TRIGGER_TYPE, 0);

        showShiftNotification(context, scheduleId, workday, shiftName, triggerType);
    }

    public static void showShiftNotification(Context context, String scheduleId, String workday,
                                             String shiftName, int triggerType) {
        if (context == null) return;

        SharedPreferences sp = context.getSharedPreferences("SHARE_PREFERENCES_APP", Context.MODE_PRIVATE);
        SharedPreferenceApp pref = new SharedPreferenceApp(sp);

        // 1. Kiểm tra xem đã chấm công chưa
        CheckState state = pref.getShiftAttendanceState(scheduleId, workday, shiftName);
        if (state == CheckState.CHECKED_IN || state == CheckState.CHECKED_OUT) {
            Log.d(TAG, "Already checked in/out for " + shiftName + ", skip notification");
            return;
        }

        // 2. Kiểm tra xem thông báo này đã được bắn hôm nay chưa (tránh spam)
        if (pref.hasShiftNotified(scheduleId, triggerType, workday)) {
            Log.d(TAG, "Notification type " + triggerType + " already sent for " + shiftName + " on " + workday);
            return;
        }

        // 3. Kiểm tra quyền POST_NOTIFICATIONS trên Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted");
                return;
            }
        }

        createNotificationChannel(context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String displayName = (shiftName != null && !shiftName.trim().isEmpty()) ? shiftName.trim() : "hiện tại";
        String title;
        String message;

        switch (triggerType) {
            case TRIGGER_ALLOWED_CHECKIN:
                title = "Đến giờ chấm công";
                message = "Ca làm việc " + displayName + " đã đến giờ chấm công, vui lòng chấm công";
                break;
            case TRIGGER_REMINDER_15:
                title = "Nhắc nhở chấm công (Quá 15p)";
                message = "Đã quá 15 phút ca làm việc " + displayName + ", bạn chưa chấm công vào. Vui lòng chấm công ngay!";
                break;
            case TRIGGER_REMINDER_30:
                title = "Cảnh báo chấm công (Quá 30p)";
                message = "Đã quá 30 phút ca làm việc " + displayName + ", bạn chưa chấm công vào. Vui lòng chấm công ngay!";
                break;
            default:
                title = "Nhắc nhở chấm công";
                message = "Ca làm việc " + displayName + " đã đến giờ chấm công, vui lòng chấm công";
                break;
        }

        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (appIntent == null) {
            appIntent = new Intent(context, AuthActivity.class);
        }
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appIntent.putExtra("EXTRA_NAVIGATE_TO", "CHECK_IN");

        int reqCode = Math.abs((scheduleId != null ? scheduleId.hashCode() : 0) * 31 + triggerType);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reqCode,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_schedule)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = Math.abs((scheduleId != null ? scheduleId.hashCode() : 1) * 100 + triggerType);
        manager.notify(notificationId, builder.build());

        // Đánh dấu đã bắn thông báo thành công
        pref.setShiftNotified(scheduleId, triggerType, workday, true);
        Log.d(TAG, "Notification successfully posted: " + message);
    }

    private static void createNotificationChannel(Context context) {
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
}
