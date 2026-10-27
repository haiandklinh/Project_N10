package com.example.attendanceapp.core.notification;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.BuildConfig;
import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.data.local.dao.NotificationDao;
import com.example.attendanceapp.data.local.entity.NotificationEntity;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@Singleton
public class WebSocketNotificationManager {

    private static final String TAG = "WebSocketNotification";
    private static final long RECONNECT_DELAY_MS = 5000L;

    private final Context context;
    private final NotificationDao notificationDao;
    private final TokenManager tokenManager;
    private final AppStateRepository appStateRepository;
    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final PublishSubject<Notification> notificationSubject = PublishSubject.create();
    private final PublishSubject<Boolean> joinApprovalSubject = PublishSubject.create();

    private WebSocket webSocket;
    private boolean isManuallyClosed = false;

    @Inject
    public WebSocketNotificationManager(
            @ApplicationContext Context context,
            NotificationDao notificationDao,
            TokenManager tokenManager,
            AppStateRepository appStateRepository
    ) {
        this.context = context;
        this.notificationDao = notificationDao;
        this.tokenManager = tokenManager;
        this.appStateRepository = appStateRepository;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }

    public Observable<Notification> observeIncomingNotifications() {
        return notificationSubject.hide();
    }

    public Observable<Boolean> observeJoinApprovalResult() {
        return joinApprovalSubject.hide();
    }

    public synchronized void connect() {
        if (webSocket != null) return;
        isManuallyClosed = false;

        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "Cannot connect WebSocket: No access token");
            return;
        }

        String wsUrl = buildWebSocketUrl();
        Log.d(TAG, "Connecting to WebSocket: " + wsUrl);

        Request request = new Request.Builder()
                .url(wsUrl)
                .header("Authorization", "Bearer " + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket connected successfully to " + wsUrl);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Log.d(TAG, "WebSocket message received: " + text);
                handleMessage(text);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
                WebSocketNotificationManager.this.webSocket = null;
                scheduleReconnect();
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                Log.w(TAG, "WebSocket failure: " + t.getMessage());
                WebSocketNotificationManager.this.webSocket = null;
                scheduleReconnect();
            }
        });
    }

    public synchronized void disconnect() {
        isManuallyClosed = true;
        if (webSocket != null) {
            webSocket.close(1000, "App closed");
            webSocket = null;
        }
    }

    public synchronized void reconnect() {
        disconnect();
        isManuallyClosed = false;
        connect();
    }

    private void scheduleReconnect() {
        if (isManuallyClosed) return;
        mainHandler.postDelayed(() -> {
            if (!isManuallyClosed && webSocket == null) {
                connect();
            }
        }, RECONNECT_DELAY_MS);
    }

    private String buildWebSocketUrl() {
        String base = BuildConfig.BASE_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.startsWith("https://")) {
            base = "wss://" + base.substring(8);
        } else if (base.startsWith("http://")) {
            base = "ws://" + base.substring(7);
        } else {
            base = "ws://" + base;
        }

        // Backend FastAPI endpoint:
        // /ws/manager/notifications cho Quản lý
        // /ws/staff/notifications cho Nhân viên
        UserRole role = appStateRepository.getUserRole();
        if (role == UserRole.EMPLOYEE) {
            return base + "/ws/staff/notifications";
        } else {
            return base + "/ws/manager/notifications";
        }
    }

    /**
     * Xử lý gói tin JSON nhận từ WebSocket Backend FastAPI.
     */
    public void handleMessage(String jsonStr) {
        try {
            JSONObject obj = new JSONObject(jsonStr);

            // Kiểm tra trường hợp backend gửi kết quả duyệt gia nhập: {"status": "True"} hoặc {"status": "False"}
            if (obj.has("status") && !obj.has("type") && !obj.has("Type")) {
                boolean isApproved = "true".equalsIgnoreCase(obj.optString("status"));
                joinApprovalSubject.onNext(isApproved);
                String title = isApproved ? "Yêu cầu gia nhập đã được duyệt" : "Yêu cầu gia nhập bị từ chối";
                String message = isApproved
                        ? "Chúc mừng! Quản lý đã phê duyệt yêu cầu gia nhập công ty của bạn."
                        : "Rất tiếc! Yêu cầu gia nhập công ty của bạn đã bị từ chối.";
                dispatchNotification(title, message, NotificationType.SYSTEM);
                return;
            }

            String event = obj.optString("event", "");
            String typeStr = obj.optString("type", obj.optString("Type", event));
            JSONObject data = obj.optJSONObject("data");
            if (data == null) {
                data = obj;
            }

            String title;
            String message;
            NotificationType type;

            switch (typeStr.toUpperCase()) {
                case "JOIN_COMPANY_REQUEST":
                    String staffName = data.optString("name_staff", "Nhân sự mới");
                    title = "Yêu cầu tham gia công ty";
                    message = staffName + " đã gửi yêu cầu xin gia nhập doanh nghiệp của bạn.";
                    type = NotificationType.SYSTEM;
                    break;

                case "ASSIGN_WORK_SCHEDULE":
                case "SCHEDULE_ASSIGNED":
                case "WORK_SCHEDULE_ASSIGNED":
                    title = "Lịch làm việc mới";
                    message = data.optString("message", data.optString("Message", "Quản lý đã xếp lịch làm việc mới cho bạn."));
                    type = NotificationType.SCHEDULE;
                    break;

                case "JOIN_COMPANY_REQUEST_RESPONSE":
                    boolean isApprovedReq = !"false".equalsIgnoreCase(data.optString("status", "true"))
                            && !data.optString("message", "").toLowerCase().contains("từ chối")
                            && !data.optString("message", "").toLowerCase().contains("reject");
                    joinApprovalSubject.onNext(isApprovedReq);
                    title = "Kết quả yêu cầu gia nhập";
                    message = data.optString("message", data.optString("Message", "Quản lý đã phản hồi yêu cầu gia nhập của bạn."));
                    type = NotificationType.SYSTEM;
                    break;

                case "CHANGE_WORK_SHIFT":
                case "SHIFT_CHANGE_REQUEST":
                case "CHANGE_WORK_SHIFT_REQUEST":
                case "NEW_CHANGE_REQUEST":
                    title = "Yêu cầu đổi ca làm việc";
                    message = data.optString("message", data.optString("Message", data.optString("reason", "Có yêu cầu đổi ca mới từ nhân viên")));
                    type = NotificationType.SCHEDULE;
                    break;

                case "CHANGE_WORK_SHIFT_RESPONSE":
                case "SHIFT_CHANGE_APPROVED":
                case "SCHEDULE_UPDATED":
                    title = "Yêu cầu đổi ca đã duyệt";
                    message = data.optString("message", data.optString("Message", "Yêu cầu đổi ca của bạn đã được phê duyệt."));
                    type = NotificationType.SCHEDULE;
                    break;

                case "CHANGE_LEAVE_REQUEST":
                case "LEAVE_REQUEST":
                    title = "Đơn xin nghỉ phép mới";
                    message = data.optString("message", data.optString("Message", data.optString("reason", "Có đơn xin nghỉ phép mới cần duyệt")));
                    type = NotificationType.LEAVE;
                    break;

                case "CHANGE_LEAVE_REQUEST_RESPONSE":
                case "LEAVE_APPROVED":
                    title = "Đơn xin nghỉ phép đã duyệt";
                    message = data.optString("message", data.optString("Message", "Đơn xin nghỉ phép của bạn đã được quản lý phê duyệt."));
                    type = NotificationType.LEAVE;
                    break;

                case "ATTENDANCE_ADJUSTMENT_REQUEST":
                    title = "Yêu cầu điều chỉnh chấm công";
                    message = data.optString("message", data.optString("Message", data.optString("reason", "Nhân viên yêu cầu điều chỉnh thời gian chấm công")));
                    type = NotificationType.ATTENDANCE;
                    break;

                case "ATTENDANCE_ADJUSTMENT_RESPONSE":
                case "ATTENDANCE_ADJUSTMENT_APPROVED":
                    title = "Điều chỉnh chấm công đã duyệt";
                    message = data.optString("message", data.optString("Message", "Yêu cầu điều chỉnh chấm công của bạn đã được phê duyệt."));
                    type = NotificationType.ATTENDANCE;
                    break;

                default:
                    title = data.optString("title", data.optString("Title", "Thông báo mới"));
                    message = data.optString("message", data.optString("Message", "Bạn có thông báo mới từ hệ thống"));
                    type = NotificationType.SYSTEM;
                    break;
            }

            dispatchNotification(title, message, type);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing WebSocket message", e);
        }
    }

    /**
     * Tạo thông báo, lưu vào Room DB, đẩy System Notification lên thanh trạng thái và phát Event.
     */
    public void dispatchNotification(String title, String message, NotificationType type) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                title,
                message,
                LocalDateTime.now(),
                type,
                false
        );

        // 1. Lưu vào Room DB bất đồng bộ
        UserRole currentRole = appStateRepository.getUserRole();
        String roleStr = currentRole != null ? currentRole.name() : null;
        notificationDao.insert(NotificationEntity.fromDomain(notification, roleStr))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "Saved notification to Room: " + notification.getId());
                }, throwable -> {
                    Log.e(TAG, "Error saving notification to Room", throwable);
                });

        // 2. Hiển thị thông báo trên thanh trạng thái / notification tray của thiết bị
        mainHandler.post(() -> {
            SystemNotificationHelper.showNotification(context, notification);
        });

        // 3. Bắn event realtime cho UI đang mở lắng nghe
        notificationSubject.onNext(notification);
    }

    // =====================================================================
    //          CÁC HÀM PHÁT SỰ KIỆN REALTIME TIỆN ÍCH (CHO WORKFLOW APP)
    // =====================================================================

    public void notifyJoinCompanyRequest(String employeeName) {
        String msg = (employeeName != null ? employeeName : "Nhân viên mới") + " đã gửi yêu cầu gia nhập công ty.";
        dispatchNotification("Yêu cầu gia nhập mới", msg, NotificationType.SYSTEM);
    }

    public void notifyShiftChangeRequest(String employeeName, String workDay, String reason) {
        String msg = (employeeName != null ? employeeName : "Nhân viên") + " gửi yêu cầu đổi ca ngày " + workDay;
        if (reason != null && !reason.trim().isEmpty()) {
            msg += ": " + reason;
        }
        dispatchNotification("Yêu cầu đổi ca mới", msg, NotificationType.SCHEDULE);
    }

    public void notifyLeaveRequest(String employeeName, String workDay, String reason) {
        String msg = (employeeName != null ? employeeName : "Nhân viên") + " xin nghỉ phép ngày " + workDay;
        if (reason != null && !reason.trim().isEmpty()) {
            msg += ": " + reason;
        }
        dispatchNotification("Đơn xin nghỉ phép mới", msg, NotificationType.LEAVE);
    }

    public void notifyAttendanceAdjustment(String employeeName, String workday, String reason) {
        String msg = (employeeName != null ? employeeName : "Nhân viên") + " yêu cầu điều chỉnh công ngày " + workday;
        if (reason != null && !reason.trim().isEmpty()) {
            msg += ": " + reason;
        }
        dispatchNotification("Yêu cầu điều chỉnh chấm công", msg, NotificationType.ATTENDANCE);
    }

    public void notifyApprovalResult(String requestType, boolean approved, String reason) {
        String title = (approved ? "Đã duyệt: " : "Từ chối: ") + requestType;
        String msg = reason != null && !reason.trim().isEmpty() ? reason : (approved ? "Yêu cầu của bạn đã được phê duyệt." : "Yêu cầu của bạn đã bị từ chối.");
        dispatchNotification(title, msg, NotificationType.SYSTEM);
    }

    public void notifyScheduleAssigned(String date) {
        String msg = "Quản lý đã xếp lịch làm việc mới cho ngày " + (date != null ? date : "sắp tới");
        dispatchNotification("Lịch làm việc mới", msg, NotificationType.SCHEDULE);
    }
}
