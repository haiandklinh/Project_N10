package com.example.attendanceapp.data.repository;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationDao notificationDao;
    private final ManagerApi managerApi;
    private final StaffApi staffApi;
    private final AppStateRepository appStateRepository;
    private final WebSocketNotificationManager webSocketManager;
    private final HandledRequestManager handledRequestManager;
    private final SessionRepository sessionRepository;

    @Inject
    public NotificationRepositoryImpl(
            NotificationDao notificationDao,
            ManagerApi managerApi,
            StaffApi staffApi,
            AppStateRepository appStateRepository,
            WebSocketNotificationManager webSocketManager,
            HandledRequestManager handledRequestManager,
            SessionRepository sessionRepository
    ) {
        this.notificationDao = notificationDao;
        this.managerApi = managerApi;
        this.staffApi = staffApi;
        this.appStateRepository = appStateRepository;
        this.webSocketManager = webSocketManager;
        this.handledRequestManager = handledRequestManager;
        this.sessionRepository = sessionRepository;

        // Đảm bảo có thông báo chào mừng mặc định duy nhất khi mở app
        ensureWelcomeNotification();

        // Khởi động kết nối WebSocket realtime khi Repository được tạo
        this.webSocketManager.connect();
    }

    private void ensureWelcomeNotification() {
        UserRole currentRole = appStateRepository.getUserRole();
        String roleStr = currentRole != null ? currentRole.name() : "";
        Flowable<List<NotificationEntity>> flowable = roleStr.isEmpty()
                ? notificationDao.observeAllNotifications()
                : notificationDao.observeNotificationsByRole(roleStr);

        flowable.firstOrError()
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    boolean hasValidWelcome = false;
                    if (list != null && !list.isEmpty()) {
                        for (NotificationEntity entity : list) {
                            if (entity.getId() != null && entity.getId().startsWith("welcome_notification_init_")) {
                                if (entity.getTitle() == null || entity.getTitle().contains("\uFFFD") || entity.getTitle().contains("?")) {
                                    entity.setTitle("Chào mừng đến với Attendance App");
                                    entity.setMessage("Chào mừng và chúc user trải nghiệm trên Attendance App tốt!");
                                    notificationDao.insert(entity).subscribeOn(Schedulers.io()).subscribe();
                                }
                                hasValidWelcome = true;
                                break;
                            }
                        }
                    }
                    if (!hasValidWelcome) {
                        NotificationEntity welcome = new NotificationEntity(
                                "welcome_notification_init_" + (roleStr.isEmpty() ? "all" : roleStr.toLowerCase()),
                                "Chào mừng đến với Attendance App",
                                "Chào mừng và chúc user trải nghiệm trên Attendance App tốt!",
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                NotificationType.SYSTEM.name(),
                                false,
                                roleStr.isEmpty() ? null : roleStr
                        );
                        notificationDao.insert(welcome).subscribeOn(Schedulers.io()).subscribe();
                    }
                }, throwable -> {});
    }

    @Override
    public Observable<List<Notification>> observeNotifications() {
        UserRole currentRole = appStateRepository.getUserRole();
        String roleStr = currentRole != null ? currentRole.name() : "";
        Flowable<List<NotificationEntity>> flowable = roleStr.isEmpty()
                ? notificationDao.observeAllNotifications()
                : notificationDao.observeNotificationsByRole(roleStr);

        return flowable
                .toObservable()
                .map(entities -> entities.stream()
                        .map(NotificationEntity::toDomain)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Integer> observeUnreadCount() {
        UserRole currentRole = appStateRepository.getUserRole();
        String roleStr = currentRole != null ? currentRole.name() : "";
        Flowable<Integer> flowable = roleStr.isEmpty()
                ? notificationDao.observeUnreadCount()
                : notificationDao.observeUnreadCountByRole(roleStr);

        return flowable
                .toObservable()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable refreshNotifications() {
        UserRole role = appStateRepository.getUserRole();
        if (role == UserRole.MANAGER) {
            return managerApi.getNotifications()
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable(res -> {
                        if (res != null && res.getData() != null) {
                            List<NotificationEntity> entities = new ArrayList<>();
                            for (com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse item : res.getData()) {
                                if (item.getStaffId() != null && handledRequestManager.isHandled(item.getStaffId())) {
                                    continue;
                                }
                                String typeStr = item.getNotificationType();
                                String senderName = item.getSendPerson() != null ? item.getSendPerson() : "Nhân viên";
                                String title;
                                String msg;
                                NotificationType type = mapNotificationType(typeStr);

                                if ("JOIN_COMPANY_REQUEST".equalsIgnoreCase(typeStr)) {
                                    title = "Yêu cầu tham gia công ty";
                                    msg = senderName + " đã gửi yêu cầu xin gia nhập doanh nghiệp của bạn.";
                                } else if ("CHANGE_WORK_SHIFT".equalsIgnoreCase(typeStr) || "SHIFT_CHANGE_REQUEST".equalsIgnoreCase(typeStr)) {
                                    title = "Yêu cầu đổi ca làm việc";
                                    msg = senderName + " gửi yêu cầu đổi ca làm việc mới.";
                                } else if ("CHANGE_LEAVE_REQUEST".equalsIgnoreCase(typeStr) || "LEAVE_REQUEST".equalsIgnoreCase(typeStr)) {
                                    title = "Đơn xin nghỉ phép mới";
                                    msg = senderName + " gửi đơn xin nghỉ phép.";
                                } else if ("ATTENDANCE_ADJUSTMENT_REQUEST".equalsIgnoreCase(typeStr)) {
                                    title = "Yêu cầu điều chỉnh chấm công";
                                    msg = senderName + " gửi yêu cầu điều chỉnh thời gian chấm công.";
                                } else {
                                    title = item.getSendPerson() != null ? item.getSendPerson() : "Thông báo";
                                    msg = "Bạn có thông báo mới từ hệ thống.";
                                    if (item.getContent() != null && item.getContent().containsKey("message")) {
                                        Object val = item.getContent().get("message");
                                        if (val != null) msg = val.toString();
                                    }
                                }

                                String time = item.getSendAt() != null ? item.getSendAt() : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                                String entityId = (item.getStaffId() != null ? item.getStaffId() : "") + "_" + (typeStr != null ? typeStr : "") + "_" + time;

                                entities.add(new NotificationEntity(
                                        entityId,
                                        title,
                                        msg,
                                        time,
                                        type.name(),
                                        false,
                                        "MANAGER"
                                ));
                            }
                            return notificationDao.insertAll(entities);
                        }
                        return Completable.complete();
                    })
                    .onErrorComplete();
        } else {
            return sessionRepository.getCurrentUser()
                    .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                    .defaultIfEmpty("")
                    .flatMapCompletable(managerId -> {
                        if (managerId.isEmpty()) {
                            return Completable.complete();
                        }
                        return staffApi.getNotifications(managerId)
                                .subscribeOn(Schedulers.io())
                                .flatMapCompletable(res -> {
                                    if (res != null && res.getData() != null) {
                                        List<NotificationEntity> entities = new ArrayList<>();
                                        for (com.example.attendanceapp.data.dto.response.staff.NotificationItemResponse item : res.getData()) {
                                            String typeStr = item.getNotificationType();
                                            String title;
                                            String msg;
                                            NotificationType type = mapNotificationType(typeStr);

                                            if ("JOIN_COMPANY_REQUEST_RESPONSE".equalsIgnoreCase(typeStr)) {
                                                title = "Kết quả yêu cầu gia nhập";
                                                msg = "Quản lý đã phản hồi yêu cầu gia nhập công ty của bạn.";
                                            } else if ("CHANGE_WORK_SHIFT_RESPONSE".equalsIgnoreCase(typeStr)) {
                                                title = "Kết quả yêu cầu đổi ca";
                                                msg = "Quản lý đã phản hồi yêu cầu đổi ca làm việc.";
                                            } else if ("CHANGE_LEAVE_REQUEST_RESPONSE".equalsIgnoreCase(typeStr)) {
                                                title = "Kết quả đơn xin nghỉ phép";
                                                msg = "Quản lý đã phản hồi đơn xin nghỉ phép của bạn.";
                                            } else if ("ATTENDANCE_ADJUSTMENT_RESPONSE".equalsIgnoreCase(typeStr)) {
                                                title = "Kết quả điều chỉnh chấm công";
                                                msg = "Quản lý đã phản hồi yêu cầu điều chỉnh chấm công.";
                                            } else {
                                                title = item.getSendPerson() != null ? item.getSendPerson() : "Thông báo";
                                                msg = "Bạn có thông báo mới từ hệ thống.";
                                                if (item.getContent() != null && item.getContent().containsKey("message")) {
                                                    Object val = item.getContent().get("message");
                                                    if (val != null) msg = val.toString();
                                                }
                                            }

                                            String time = item.getSendAt() != null ? item.getSendAt() : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                                            String entityId = UUID.randomUUID().toString();

                                            entities.add(new NotificationEntity(
                                                    entityId,
                                                    title,
                                                    msg,
                                                    time,
                                                    type.name(),
                                                    false,
                                                    "EMPLOYEE"
                                            ));
                                        }
                                        return notificationDao.insertAll(entities);
                                    }
                                    return Completable.complete();
                                });
                    })
                    .onErrorComplete();
        }
    }

    private NotificationType mapNotificationType(String typeStr) {
        if (typeStr == null) return NotificationType.SYSTEM;
        switch (typeStr.toUpperCase()) {
            case "CHANGE_LEAVE_REQUEST":
            case "CHANGE_LEAVE_REQUEST_RESPONSE":
            case "LEAVE_REQUEST":
            case "LEAVE":
                return NotificationType.LEAVE;
            case "CHANGE_WORK_SHIFT":
            case "CHANGE_WORK_SHIFT_REQUEST":
            case "CHANGE_WORK_SHIFT_RESPONSE":
            case "SHIFT_CHANGE_REQUEST":
            case "SHIFT_CHANGE_APPROVED":
            case "SCHEDULE_UPDATED":
            case "SCHEDULE":
                return NotificationType.SCHEDULE;
            case "ATTENDANCE_ADJUSTMENT_REQUEST":
            case "ATTENDANCE_ADJUSTMENT_RESPONSE":
            case "ATTENDANCE":
            case "REMINDER":
                return NotificationType.ATTENDANCE;
            case "PAYROLL":
                return NotificationType.PAYROLL;
            case "JOIN_COMPANY_REQUEST":
            case "JOIN_COMPANY_REQUEST_RESPONSE":
            default:
                return NotificationType.SYSTEM;
        }
    }

    @Override
    public Completable markAsRead(String id) {
        return notificationDao.markAsRead(id)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable markAllAsRead() {
        UserRole currentRole = appStateRepository.getUserRole();
        String roleStr = currentRole != null ? currentRole.name() : "";
        return (roleStr.isEmpty() ? notificationDao.markAllAsRead() : notificationDao.markAllAsReadByRole(roleStr))
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
