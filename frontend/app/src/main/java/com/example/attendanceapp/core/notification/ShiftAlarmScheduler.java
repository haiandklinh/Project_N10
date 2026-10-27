package com.example.attendanceapp.core.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.model.Shift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ShiftAlarmScheduler {

    private static final String TAG = "ShiftAlarmScheduler";

    private ShiftAlarmScheduler() {}

    /**
     * Lên lịch thông báo và nhắc nhở chấm công cho ca làm việc hôm nay:
     * 1. Đến giờ cho phép chấm công do quản lý đặt: Bắn thông báo đến giờ chấm công (hoặc bắn ngay nếu giờ đó đã tới).
     * 2. Quá 15 phút từ giờ bắt đầu ca: Bắn thông báo nhắc nhở nếu chưa chấm công.
     * 3. Quá 30 phút từ giờ bắt đầu ca: Bắn thông báo nhắc nhở tiếp theo nếu chưa chấm công.
     */
    public static void scheduleShiftAlarms(Context context, Shift shift, SharedPreferenceApp pref) {
        if (context == null || shift == null || !shift.isToday()) return;

        // Nếu đã chấm công ca này rồi thì không lên lịch nhắc nữa
        if (pref != null) {
            CheckState state = pref.getShiftAttendanceState(shift.getId(), shift.getDate().toString(), shift.getShiftName());
            if (state == CheckState.CHECKED_IN || state == CheckState.CHECKED_OUT) {
                Log.d(TAG, "Shift " + shift.getShiftName() + " already checked in/out, skipping alarms");
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        LocalDate date = shift.getDate();
        LocalTime startTime = shift.getStartTime();
        LocalTime allowedCheckIn = shift.getAllowedCheckInTime();
        if (allowedCheckIn == null) {
            allowedCheckIn = startTime;
        }

        long nowMillis = System.currentTimeMillis();

        // 1. Thông báo khi đến giờ cho phép chấm công
        if (allowedCheckIn != null) {
            LocalDateTime triggerTime1 = LocalDateTime.of(date, allowedCheckIn);
            long millis1 = triggerTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (millis1 > nowMillis) {
                // Thời gian ở tương lai: Đặt chuông báo chính xác
                scheduleAlarm(context, alarmManager, millis1, shift, ShiftNotificationReceiver.TRIGGER_ALLOWED_CHECKIN);
            } else {
                // Thời gian cho phép chấm công đã tới (hoặc ca đang diễn ra): Bắn thông báo ngay nếu chưa bắn
                ShiftNotificationReceiver.showShiftNotification(
                        context, shift.getId(), date.toString(), shift.getShiftName(),
                        ShiftNotificationReceiver.TRIGGER_ALLOWED_CHECKIN
                );
            }
        }

        // 2. Nhắc nhở sau 15 phút nếu chưa chấm công
        if (startTime != null) {
            LocalDateTime triggerTime2 = LocalDateTime.of(date, startTime.plusMinutes(15));
            long millis2 = triggerTime2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (millis2 > nowMillis) {
                scheduleAlarm(context, alarmManager, millis2, shift, ShiftNotificationReceiver.TRIGGER_REMINDER_15);
            } else if (nowMillis >= millis2 && nowMillis < millis2 + 15 * 60 * 1000L) {
                ShiftNotificationReceiver.showShiftNotification(
                        context, shift.getId(), date.toString(), shift.getShiftName(),
                        ShiftNotificationReceiver.TRIGGER_REMINDER_15
                );
            }
        }

        // 3. Nhắc nhở sau 30 phút nếu chưa chấm công
        if (startTime != null) {
            LocalDateTime triggerTime3 = LocalDateTime.of(date, startTime.plusMinutes(30));
            long millis3 = triggerTime3.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (millis3 > nowMillis) {
                scheduleAlarm(context, alarmManager, millis3, shift, ShiftNotificationReceiver.TRIGGER_REMINDER_30);
            } else if (nowMillis >= millis3 && (shift.getEndTime() == null || nowMillis <= LocalDateTime.of(date, shift.getEndTime()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())) {
                ShiftNotificationReceiver.showShiftNotification(
                        context, shift.getId(), date.toString(), shift.getShiftName(),
                        ShiftNotificationReceiver.TRIGGER_REMINDER_30
                );
            }
        }
    }

    private static void scheduleAlarm(Context context, AlarmManager alarmManager, long triggerMillis,
                                      Shift shift, int triggerType) {
        Intent intent = new Intent(context, ShiftNotificationReceiver.class);
        intent.setAction(ShiftNotificationReceiver.ACTION_SHIFT_NOTIFICATION);
        intent.putExtra(ShiftNotificationReceiver.EXTRA_SCHEDULE_ID, shift.getId());
        intent.putExtra(ShiftNotificationReceiver.EXTRA_WORKDAY, shift.getDate().toString());
        intent.putExtra(ShiftNotificationReceiver.EXTRA_SHIFT_NAME, shift.getShiftName());
        intent.putExtra(ShiftNotificationReceiver.EXTRA_TRIGGER_TYPE, triggerType);

        int reqCode = getRequestCode(shift.getId(), shift.getShiftName(), triggerType);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            }
            Log.d(TAG, "Scheduled shift alarm type " + triggerType + " at " + triggerMillis + " for " + shift.getShiftName());
        } catch (SecurityException e) {
            Log.w(TAG, "Cannot schedule exact alarm: " + e.getMessage());
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        }
    }

    /**
     * Hủy bỏ tất cả các chuông nhắc nhở của ca làm việc (gọi khi nhân viên thực hiện check-in thành công)
     */
    public static void cancelShiftAlarms(Context context, String scheduleId, String shiftName) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        cancelAlarm(context, alarmManager, scheduleId, shiftName, ShiftNotificationReceiver.TRIGGER_ALLOWED_CHECKIN);
        cancelAlarm(context, alarmManager, scheduleId, shiftName, ShiftNotificationReceiver.TRIGGER_REMINDER_15);
        cancelAlarm(context, alarmManager, scheduleId, shiftName, ShiftNotificationReceiver.TRIGGER_REMINDER_30);
    }

    private static void cancelAlarm(Context context, AlarmManager alarmManager, String scheduleId,
                                    String shiftName, int triggerType) {
        Intent intent = new Intent(context, ShiftNotificationReceiver.class);
        intent.setAction(ShiftNotificationReceiver.ACTION_SHIFT_NOTIFICATION);

        int reqCode = getRequestCode(scheduleId, shiftName, triggerType);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Cancelled shift alarm type " + triggerType + " for " + shiftName);
        }
    }

    private static int getRequestCode(String scheduleId, String shiftName, int triggerType) {
        int code = 17;
        if (scheduleId != null) code = 31 * code + scheduleId.hashCode();
        if (shiftName != null) code = 31 * code + shiftName.hashCode();
        return Math.abs(31 * code + triggerType);
    }
}
