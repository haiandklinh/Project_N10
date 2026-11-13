package com.example.attendanceapp.data.repository;

import android.content.Context;

import com.example.attendanceapp.core.notification.NotificationRolePolicy;
import com.example.attendanceapp.core.notification.SystemNotificationHelper;
import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.data.local.HandledRequestManager;
import com.example.attendanceapp.data.local.dao.NotificationDao;
import com.example.attendanceapp.data.local.entity.NotificationEntity;
import com.example.attendanceapp.data.remote.ManagerApi;
import com.example.attendanceapp.data.remote.StaffApi;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class NotificationRepositoryImpl implements NotificationRepository {

    private final Context context;
    private final NotificationDao notificationDao;
    private final ManagerApi managerApi;
    private final StaffApi staffApi;
    private final AppStateRepository appStateRepository;
    private final WebSocketNotificationManager webSocketManager;
    private final HandledRequestManager handledRequestManager;
    private final SessionRepository sessionRepository;

    @Inject
    public NotificationRepositoryImpl(
            @ApplicationContext Context context,
            NotificationDao notificationDao,
            ManagerApi managerApi,
            StaffApi staffApi,
            AppStateRepository appStateRepository,
            WebSocketNotificationManager webSocketManager,
            HandledRequestManager handledRequestManager,
            SessionRepository sessionRepository
    ) {
        this.context = context;
        this.notificationDao = notificationDao;
        this.managerApi = managerApi;
        this.staffApi = staffApi;
        this.appStateRepository = appStateRepository;
        this.webSocketManager = webSocketManager;
        this.handledRequestManager = handledRequestManager;
        this.sessionRepository = sessionRepository;

        // Dọn thông báo không có role hoặc bị lưu nhầm vai trò từ phiên bản trước
        cleanupMisroutedNotifications();

        // Đảm bảo có thông báo chào mừng mặc định theo đúng vai trò hiện tại
        ensureWelcomeNotification(appStateRepository.getUserRole());

        // Khởi động kết nối WebSocket realtime khi Repository được tạo
        this.webSocketManager.connect();
    }

    private void cleanupMisroutedNotifications() {
        notificationDao.deleteUnscopedNotifications()
                .andThen(notificationDao.deleteCrossRoleNotificationsForManager())
                .andThen(notificationDao.deleteCrossRoleNotificationsForEmployee())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, throwable -> {});
    }

    private void ensureWelcomeNotification(UserRole currentRole) {
        if (currentRole == null) return;
        String roleStr = currentRole.name();

        notificationDao.observeNotificationsByRole(roleStr)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    boolean hasValidWelcome = false;
                    if (list != null && !list.isEmpty()) {
                        for (NotificationEntity entity : list) {
                            if (entity.getId() != null && entity.getId().startsWith("welcome_notification_init_")) {
                                if (entity.getTitle() == null || entity.getTitle().contains("\uFFFD") || entity.getTitle().contains("?")) {
                                    entity.setTitle("Chào mừng đến với Attendance App");
                                    entity.setMessage("Rất vui khi có bạn đồng hành! Attendance App sẽ giúp bạn theo dõi lịch làm việc và quản lý thời gian hiệu quả hơn!");
                                    notificationDao.insert(entity).subscribeOn(Schedulers.io()).subscribe();
                                }
                                hasValidWelcome = true;
                                break;
                            }
                        }
                    }
                    if (!hasValidWelcome) {
                        NotificationEntity welcome = new NotificationEntity(
                                "welcome_notification_init_" + roleStr.toLowerCase(),
                                "Chào mừng đến với Attendance App",
                                "Rất vui khi có bạn đồng hành! Attendance App sẽ giúp bạn theo dõi lịch làm việc và quản lý thời gian hiệu quả hơn!",
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                NotificationType.SYSTEM.name(),
                                false,
                                roleStr
                        );
                        notificationDao.insert(welcome).subscribeOn(Schedulers.io()).subscribe();
                    }
                }, throwable -> {});
    }

    @Override
    public Observable<List<Notification>> observeNotifications() {
        UserRole currentRole = appStateRepository.getUserRole();
        if (currentRole == null) {
            return Observable.just(Collections.emptyList());
        }

        return notificationDao.observeNotificationsByRole(currentRole.name())
                .toObservable()
                .map(entities -> entities.stream()
                        .map(NotificationEntity::toDomain)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Integer> observeUnreadCount() {
        UserRole currentRole = appStateRepository.getUserRole();
        if (currentRole == null) {
            return Observable.just(0);
        }

        return notificationDao.observeUnreadCountByRole(currentRole.name())
                .toObservable()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable refreshNotifications() {
        UserRole role = appStateRepository.getUserRole();
        if (role == UserRole.MANAGER) {
            ensureWelcomeNotification(UserRole.MANAGER);
            return notificationDao.deleteCrossRoleNotificationsForManager()
                    .andThen(notificationDao.getAllNotificationIdsByRole("MANAGER")
                            .onErrorReturnItem(Collections.emptyList())
                            .flatMapCompletable(existingIdsList -> {
                                Set<String> existingIds = new HashSet<>(existingIdsList);
                                return notificationDao.getReadNotificationIdsByRole("MANAGER")
                                        .onErrorReturnItem(Collections.emptyList())
                                        .flatMapCompletable(readIdList -> {
                                            Set<String> readIds = new HashSet<>(readIdList);
                                            return managerApi.getNotifications()
                                                    .subscribeOn(Schedulers.io())
                                                    .flatMapCompletable(res -> {
                                                        if (res != null && res.getData() != null) {
                                                            List<NotificationEntity> entities = new ArrayList<>();
                                                            List<Notification> newNotificationsToNotify = new ArrayList<>();
                                                            for (com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse item : res.getData()) {
                                                                if (item == null) continue;
                                                                String typeStr = item.getNotificationType();
                                                                if (!NotificationRolePolicy.isAllowedForRole(typeStr, UserRole.MANAGER)) {
                                                                    continue;
                                                                }
                                                                if ("JOIN_COMPANY_REQUEST".equalsIgnoreCase(typeStr)
                                                                        && item.getStaffId() != null
                                                                        && handledRequestManager.isHandled(item.getStaffId())) {
                                                                    continue;
                                                                }
                                                                String senderName = item.getSendPerson() != null ? item.getSendPerson() : "Nhân viên";
                                                                String title;
                                                                String msg;
                                                                NotificationType type = NotificationRolePolicy.mapNotificationType(typeStr);

                                                                if ("JOIN_COMPANY_REQUEST".equalsIgnoreCase(typeStr)) {
                                                                    title = "Yêu cầu tham gia công ty";
                                                                    msg = senderName + " đã gửi yêu cầu xin gia nhập doanh nghiệp của bạn.";
                                                                } else if ("CHANGE_WORK_SHIFT".equalsIgnoreCase(typeStr)
                                                                        || "SHIFT_CHANGE_REQUEST".equalsIgnoreCase(typeStr)
                                                                        || "CHANGE_WORK_SHIFT_REQUEST".equalsIgnoreCase(typeStr)
                                                                        || "NEW_CHANGE_REQUEST".equalsIgnoreCase(typeStr)
                                                                        || "WORK_SCHEDULE_REQUEST".equalsIgnoreCase(typeStr)) {
                                                                    title = "Yêu cầu đổi ca làm việc";
                                                                    msg = senderName + " gửi yêu cầu đổi ca làm việc mới.";
                                                                } else if ("CHANGE_LEAVE_REQUEST".equalsIgnoreCase(typeStr)
                                                                        || "LEAVE_REQUEST".equalsIgnoreCase(typeStr)) {
                                                                    title = "Đơn xin nghỉ phép mới";
                                                                    msg = senderName + " gửi đơn xin nghỉ phép.";
                                                                } else if ("ATTENDANCE_ADJUSTMENT_REQUEST".equalsIgnoreCase(typeStr)) {
                                                                    title = "Yêu cầu điều chỉnh chấm công";
                                                                    msg = senderName + " gửi yêu cầu điều chỉnh thời gian chấm công.";
                                                                } else {
                                                                    title = item.getSendPerson() != null ? item.getSendPerson() : "Thông báo";
                                                                    msg = extractContentMessage(item.getContent(), "Bạn có thông báo mới từ hệ thống.");
                                                                }

                                                                String time = normalizeServerTimestamp(item.getSendAt());
                                                                String entityId = (item.getNotificationId() != null && !item.getNotificationId().trim().isEmpty())
                                                                        ? item.getNotificationId().trim()
                                                                        : (item.getStaffId() != null ? item.getStaffId() : "") + "_" + (typeStr != null ? typeStr : "") + "_" + time;

                                                                NotificationEntity entity = new NotificationEntity(
                                                                        entityId,
                                                                        title,
                                                                        msg,
                                                                        time,
                                                                        type.name(),
                                                                        readIds.contains(entityId),
                                                                        "MANAGER"
                                                                );
                                                                entities.add(entity);

                                                                // Kiểm tra nếu thông báo này chưa từng có trong DB và chưa đọc
                                                                if (!existingIds.contains(entityId) && !readIds.contains(entityId)) {
                                                                    newNotificationsToNotify.add(entity.toDomain());
                                                                }
                                                            }

                                                            // Bắn thông báo hệ thống cho các thông báo mới nhận được (tối đa 3 thông báo mới nhất)
                                                            if (!existingIds.isEmpty()) {
                                                                int count = 0;
                                                                for (Notification notif : newNotificationsToNotify) {
                                                                    if (count++ >= 3) break;
                                                                    SystemNotificationHelper.showNotification(context, notif);
                                                                }
                                                            }

                                                            return notificationDao.insertAll(entities);
                                                        }
                                                        return Completable.complete();
                                                    });
                                        });
                            }))
                    .onErrorComplete();
        } else if (role == UserRole.EMPLOYEE) {
            ensureWelcomeNotification(UserRole.EMPLOYEE);
            return notificationDao.deleteCrossRoleNotificationsForEmployee()
                    .andThen(sessionRepository.getCurrentUser()
                            .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                            .defaultIfEmpty("")
                            .flatMapCompletable(managerId -> {
                                if (managerId.isEmpty()) {
                                    return Completable.complete();
                                }
                                return notificationDao.getAllNotificationIdsByRole("EMPLOYEE")
                                        .onErrorReturnItem(Collections.emptyList())
                                        .flatMapCompletable(existingIdsList -> {
                                            Set<String> existingIds = new HashSet<>(existingIdsList);
                                            return notificationDao.getReadNotificationIdsByRole("EMPLOYEE")
                                                    .onErrorReturnItem(Collections.emptyList())
                                                    .flatMapCompletable(readIdList -> {
                                                        Set<String> readIds = new HashSet<>(readIdList);
                                                        return staffApi.getNotifications(managerId)
                                                                .subscribeOn(Schedulers.io())
                                                                .flatMapCompletable(res -> {
                                                                    if (res != null && res.getData() != null) {
                                                                        List<NotificationEntity> entities = new ArrayList<>();
                                                                        List<Notification> newNotificationsToNotify = new ArrayList<>();
                                                                        for (com.example.attendanceapp.data.dto.response.staff.NotificationItemResponse item : res.getData()) {
                                                                            if (item == null) continue;
                                                                            String typeStr = item.getNotificationType();
                                                                            if (!isVisibleToEmployee(typeStr, item.getContent())) {
                                                                                continue;
                                                                            }

                                                                            String title;
                                                                            String msg;
                                                                            NotificationType type = NotificationRolePolicy.mapNotificationType(typeStr);
                                                                            boolean approvalResponse = isApprovalResponseType(typeStr, item.getContent());
                                                                            String upperType = typeStr != null ? typeStr.toUpperCase(Locale.US) : "";

                                                                            if ("ASSIGN_WORK_SCHEDULE".equalsIgnoreCase(typeStr)
                                                                                    || "SCHEDULE_ASSIGNED".equalsIgnoreCase(typeStr)
                                                                                    || "WORK_SCHEDULE_ASSIGNED".equalsIgnoreCase(typeStr)) {
                                                                                title = "Lịch làm việc mới";
                                                                                msg = buildScheduleAssignedMessage(item.getContent());
                                                                            } else if ("SCHEDULE_UPDATED".equalsIgnoreCase(typeStr)) {
                                                                                title = "Cập nhật lịch làm việc";
                                                                                msg = extractContentMessage(item.getContent(), "Lịch làm việc của bạn đã được cập nhật.");
                                                                            } else if (approvalResponse && (upperType.contains("SHIFT") || upperType.contains("WORK_SCHEDULE") || upperType.contains("CHANGE_WORK"))) {
                                                                                title = "Kết quả yêu cầu đổi ca";
                                                                                msg = extractContentMessage(item.getContent(), "Quản lý đã phản hồi yêu cầu đổi ca làm việc.");
                                                                            } else if (approvalResponse && (upperType.contains("LEAVE") || upperType.contains("OFF"))) {
                                                                                title = "Kết quả đơn xin nghỉ phép";
                                                                                msg = extractContentMessage(item.getContent(), "Quản lý đã phản hồi đơn xin nghỉ phép của bạn.");
                                                                            } else if (approvalResponse && upperType.contains("ADJUSTMENT")) {
                                                                                title = "Kết quả điều chỉnh chấm công";
                                                                                msg = extractContentMessage(item.getContent(), "Quản lý đã phản hồi yêu cầu điều chỉnh chấm công.");
                                                                            } else if ("JOIN_COMPANY_REQUEST_RESPONSE".equalsIgnoreCase(typeStr)) {
                                                                                title = "Kết quả yêu cầu gia nhập";
                                                                                msg = extractContentMessage(item.getContent(), "Quản lý đã phản hồi yêu cầu gia nhập công ty của bạn.");
                                                                            } else if ("PAYROLL".equalsIgnoreCase(typeStr)) {
                                                                                title = "Thông báo bảng lương";
                                                                                msg = extractContentMessage(item.getContent(), "Bảng lương của bạn đã được cập nhật.");
                                                                            } else {
                                                                                title = item.getSendPerson() != null ? item.getSendPerson() : "Thông báo";
                                                                                msg = extractContentMessage(item.getContent(), "Bạn có thông báo mới từ hệ thống.");
                                                                            }

                                                                            String time = normalizeServerTimestamp(item.getSendAt());
                                                                            String entityId = (item.getId() != null && !item.getId().trim().isEmpty())
                                                                                    ? "emp_" + item.getId().trim()
                                                                                    : "emp_" + (typeStr != null ? typeStr : "") + "_" + time + "_" + (title + "|" + msg).hashCode();

                                                                            NotificationEntity entity = new NotificationEntity(
                                                                                    entityId,
                                                                                    title,
                                                                                    msg,
                                                                                    time,
                                                                                    type.name(),
                                                                                    readIds.contains(entityId),
                                                                                    "EMPLOYEE"
                                                                            );
                                                                            entities.add(entity);

                                                                            if (!existingIds.contains(entityId) && !readIds.contains(entityId)) {
                                                                                newNotificationsToNotify.add(entity.toDomain());
                                                                            }
                                                                        }

                                                                        if (!existingIds.isEmpty()) {
                                                                            int count = 0;
                                                                            for (Notification notif : newNotificationsToNotify) {
                                                                                if (count++ >= 3) break;
                                                                                SystemNotificationHelper.showNotification(context, notif);
                                                                            }
                                                                        }

                                                                        return notificationDao.insertAll(entities);
                                                                    }
                                                                    return Completable.complete();
                                                                });
                                                    });
                                        });
                            }))
                    .onErrorComplete();
        } else {
            return Completable.complete();
        }
    }

    private boolean isApprovalResponseType(String typeStr, Map<String, Object> content) {
        if (typeStr == null) return false;
        String upper = typeStr.trim().toUpperCase(Locale.US);
        if (upper.endsWith("_RESPONSE") || upper.endsWith("_APPROVED") || upper.endsWith("_REJECTED")
                || upper.contains("RESPONSE") || upper.contains("APPROVED") || upper.contains("REJECTED")) {
            return true;
        }
        return content != null && content.containsKey("status");
    }

    private boolean isVisibleToEmployee(String typeStr, Map<String, Object> content) {
        if (NotificationRolePolicy.isAllowedForRole(typeStr, UserRole.EMPLOYEE)) {
            return true;
        }
        // Một số backend dùng chung notification_type cho request/response và chỉ phân biệt bằng content.status.
        return isApprovalResponseType(typeStr, content);
    }

    private String extractContentMessage(Map<String, Object> content, String defaultMessage) {
        if (content != null) {
            Object val = content.containsKey("message") ? content.get("message") : content.get("Message");
            if (val != null && !val.toString().trim().isEmpty()) {
                return val.toString().trim();
            }
        }
        return defaultMessage;
    }

    /**
     * Chuẩn hóa thời gian gửi từ server về giờ địa phương của thiết bị.
     * Server thường trả UTC (ISO có Z/+00:00 hoặc chuỗi naive yyyy-MM-dd HH:mm:ss),
     * nếu lưu nguyên sẽ bị hiển thị lệch đúng bằng múi giờ (VD: UTC+7 -> "7 giờ trước").
     */
    private String normalizeServerTimestamp(String sendAt) {
        if (sendAt == null || sendAt.trim().isEmpty()) {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        String time = sendAt.trim();

        if (time.matches("\\d{10,13}")) {
            try {
                long millis = Long.parseLong(time);
                if (time.length() == 10) {
                    millis *= 1000L;
                }
                return Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {
            }
        }

        try {
            return OffsetDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime utc = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
            return utc.atZone(ZoneOffset.UTC)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }

        String[] utcPatterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"};
        for (String pattern : utcPatterns) {
            try {
                LocalDateTime utc = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(pattern));
                return utc.atZone(ZoneOffset.UTC)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException ignored) {
            }
        }

        String[] localPatterns = {"dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm"};
        for (String pattern : localPatterns) {
            try {
                return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(pattern))
                        .format(DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException ignored) {
            }
        }

        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private String buildScheduleAssignedMessage(Map<String, Object> content) {
        String customMsg = extractContentMessage(content, "");
        if (!customMsg.isEmpty()) {
            return customMsg;
        }
        if (content != null) {
            Object workday = content.get("workday") != null ? content.get("workday") : content.get("work_day");
            Object shiftName = content.get("shift_name") != null ? content.get("shift_name") : content.get("name_shift");
            if (workday != null && shiftName != null) {
                return "Quản lý đã xếp cho bạn ca " + shiftName + " vào ngày " + workday + ".";
            }
            if (workday != null) {
                return "Quản lý đã xếp lịch làm việc mới cho ngày " + workday + ".";
            }
        }
        return "Quản lý đã xếp lịch làm việc mới cho bạn.";
    }

    @Override
    public Completable markAsRead(String id) {
        return notificationDao.markAsRead(id)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable markAllAsRead() {
        UserRole currentRole = appStateRepository.getUserRole();
        if (currentRole == null) {
            return Completable.complete();
        }
        return notificationDao.markAllAsReadByRole(currentRole.name())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void postNotification(Notification notification) {
        if (notification == null) return;
        webSocketManager.dispatchNotification(
                notification.getTitle(),
                notification.getMessage(),
                notification.getType()
        );
    }

    @Override
    public void notifyShiftChangeRequest(String employeeName, String workDay, String reason) {
        webSocketManager.notifyShiftChangeRequest(employeeName, workDay, reason);
    }

    @Override
    public void notifyLeaveRequest(String employeeName, String workDay, String reason) {
        webSocketManager.notifyLeaveRequest(employeeName, workDay, reason);
    }

    @Override
    public void notifyAttendanceAdjustment(String employeeName, String workday, String reason) {
        webSocketManager.notifyAttendanceAdjustment(employeeName, workday, reason);
    }

    @Override
    public void notifyApprovalResult(String requestType, boolean approved, String reason) {
        webSocketManager.notifyApprovalResult(requestType, approved, reason);
    }

    @Override
    public Completable deleteNotificationsByStaffId(String staffId) {
        return notificationDao.deleteByStaffId(staffId)
                .subscribeOn(Schedulers.io());
    }
}
