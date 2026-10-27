package com.example.attendanceapp.ui.manager.human;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.data.dto.request.manager.HandleJoinRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.CompanyInvitationResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.data.local.HandledRequestManager;
import com.example.attendanceapp.data.local.StaffAvatarCache;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.JoinRequestItem;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerHumanViewModel extends ViewModel {

    private final Context appContext;
    private final Set<String> processingStaffIds = new HashSet<>();

    private final ManagerRepository managerRepository;
    private final SessionRepository sessionRepository;
    private final HandledRequestManager handledRequestManager;
    private final NotificationRepository notificationRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<String> _employeeSearchQuery = new MutableLiveData<>("");
    public LiveData<String> employeeSearchQuery = _employeeSearchQuery;

    private final MutableLiveData<String> _departmentSearchQuery = new MutableLiveData<>("");
    public LiveData<String> departmentSearchQuery = _departmentSearchQuery;

    private final MutableLiveData<String> _positionSearchQuery = new MutableLiveData<>("");
    public LiveData<String> positionSearchQuery = _positionSearchQuery;

    private final MutableLiveData<Integer> _joinRequestCount = new MutableLiveData<>(0);
    public LiveData<Integer> joinRequestCount = _joinRequestCount;

    private final MutableLiveData<List<JoinRequestItem>> _joinRequests = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<JoinRequestItem>> joinRequests = _joinRequests;

    private final MutableLiveData<List<ManagerEmployeeItem>> _employees = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<ManagerEmployeeItem>> employees = _employees;

    private final MutableLiveData<String> _invitationCode = new MutableLiveData<>(null);
    public LiveData<String> invitationCode = _invitationCode;

    private final MutableLiveData<Boolean> _isGeneratingCode = new MutableLiveData<>(false);
    public LiveData<Boolean> isGeneratingCode = _isGeneratingCode;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<CompanyInvitationResponse> _generateCodeSuccess = new MutableLiveData<>(null);
    public LiveData<CompanyInvitationResponse> generateCodeSuccess = _generateCodeSuccess;

    @Inject
    public ManagerHumanViewModel(
            ManagerRepository managerRepository,
            SessionRepository sessionRepository,
            HandledRequestManager handledRequestManager,
            NotificationRepository notificationRepository,
            WebSocketNotificationManager webSocketManager,
            @ApplicationContext Context context) {
        this.managerRepository = managerRepository;
        this.sessionRepository = sessionRepository;
        this.handledRequestManager = handledRequestManager;
        this.notificationRepository = notificationRepository;
        this.appContext = context;
        _joinRequestCount.setValue(0);

        loadInitialInvitationCode();
        loadStaffs();
        loadJoinRequests();

        disposables.add(webSocketManager.observeIncomingNotifications()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notification -> {
                    loadStaffs();
                    loadJoinRequests();
                }, throwable -> {}));
    }

    private void loadInitialInvitationCode() {
        // Ưu tiên đọc từ cache Room
        disposables.add(sessionRepository.getCachedInvitationCode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        code -> {
                            if (code != null && !code.isEmpty()) {
                                _invitationCode.setValue(code);
                            }
                        },
                        throwable -> {}
                ));

        // Theo dõi thay đổi của UserEntity để đồng bộ mã mời realtime
        disposables.add(sessionRepository.observeCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (user != null && user.getInvitationCode() != null && !user.getInvitationCode().isEmpty()) {
                                _invitationCode.setValue(user.getInvitationCode());
                            }
                        },
                        throwable -> {}
                ));
    }

    /**
     * Tải danh sách đơn xin gia nhập công ty từ Backend Notifications
     * Đồng thời đối chiếu danh sách nhân sự thực tế từ GET /managers/staffs để lọc bỏ triệt để
     * những nhân viên đã ở trong công ty (tránh hiện lại request đã duyệt/xử lý).
     */
    public void loadJoinRequests() {
        disposables.add(managerRepository.getStaffs()
                .subscribeOn(Schedulers.io())
                .flatMap(staffsResponse -> {
                    Set<String> existingStaffIds = new HashSet<>();
                    if (staffsResponse != null && staffsResponse.getData() != null) {
                        for (StaffItemResponse staff : staffsResponse.getData()) {
                            if (staff.getStaffId() != null) {
                                existingStaffIds.add(staff.getStaffId());
                                handledRequestManager.markHandled(staff.getStaffId());
                            }
                        }
                    }
                    return managerRepository.getNotifications()
                            .map(notifResponse -> new Object[]{existingStaffIds, notifResponse});
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            Set<String> existingStaffIds = (Set<String>) result[0];
                            BaseListResponse<NotificationItemResponse> response = (BaseListResponse<NotificationItemResponse>) result[1];
                            List<JoinRequestItem> requests = new ArrayList<>();
                            if (response != null && response.getData() != null) {
                                for (NotificationItemResponse item : response.getData()) {
                                    if ("JOIN_COMPANY_REQUEST".equalsIgnoreCase(item.getNotificationType())) {
                                        if ("deactive".equalsIgnoreCase(item.getStatus())) {
                                            continue;
                                        }

                                        String staffId = item.getStaffId();
                                        if (staffId != null && (existingStaffIds.contains(staffId)
                                                || handledRequestManager.isHandled(staffId)
                                                || isStaffInCompany(staffId))) {
                                            continue;
                                        }

                                        String name = item.getSendPerson() != null && !item.getSendPerson().trim().isEmpty()
                                                ? item.getSendPerson() : "Nhân sự mới";

                                        String avatar = item.getSendPersonAvatar();
                                        if (avatar != null && (avatar.equalsIgnoreCase("null") || avatar.equalsIgnoreCase("none"))) {
                                            avatar = null;
                                        }
                                        if ((avatar == null || avatar.trim().isEmpty()) && appContext != null) {
                                            avatar = StaffAvatarCache.getAvatarByName(appContext, name);
                                        }

                                        String time = item.getSendAt() != null ? item.getSendAt() : "Mới đây";
                                        String code = _invitationCode.getValue() != null ? _invitationCode.getValue() : "MÃ MỜI";
                                        String phone = item.getPhoneNumber() != null ? item.getPhoneNumber() : "";

                                        requests.add(new JoinRequestItem(
                                                staffId,
                                                name,
                                                avatar,
                                                phone,
                                                code,
                                                time,
                                                ApprovalStatus.PENDING
                                        ));
                                    }
                                }
                            }
                            _joinRequests.setValue(requests);
                            _joinRequestCount.setValue(requests.size());
                        },
                        throwable -> {
                            _joinRequests.setValue(new ArrayList<>());
                            _joinRequestCount.setValue(0);
                        }
                ));
    }

    private void filterOutExistingEmployeesFromJoinRequests() {
        List<JoinRequestItem> current = _joinRequests.getValue();
        if (current == null || current.isEmpty()) return;
        List<JoinRequestItem> filtered = new ArrayList<>();
        for (JoinRequestItem item : current) {
            if (!isStaffInCompany(item.getId()) && !handledRequestManager.isHandled(item.getId())) {
                filtered.add(item);
            }
        }
        if (filtered.size() != current.size()) {
            _joinRequests.setValue(filtered);
            _joinRequestCount.setValue(filtered.size());
        }
    }

    private void removeStaffFromJoinRequests(String staffId) {
        if (staffId == null) return;
        List<JoinRequestItem> current = _joinRequests.getValue();
        if (current != null && !current.isEmpty()) {
            List<JoinRequestItem> updated = new ArrayList<>();
            for (JoinRequestItem it : current) {
                if (!staffId.equals(it.getId())) {
                    updated.add(it);
                }
            }
            if (updated.size() != current.size()) {
                _joinRequests.setValue(updated);
                _joinRequestCount.setValue(updated.size());
            }
        }
    }

    private boolean isStaffInCompany(String staffId) {
        if (staffId == null) return false;
        List<ManagerEmployeeItem> currentStaffs = _employees.getValue();
        if (currentStaffs != null) {
            for (ManagerEmployeeItem emp : currentStaffs) {
                if (staffId.equals(emp.getUserId())) return true;
            }
        }
        return false;
    }

    /**
     * Tải danh sách nhân viên thực tế của công ty từ GET /managers/staffs
     */
    public void loadStaffs() {
        disposables.add(managerRepository.getStaffs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            List<ManagerEmployeeItem> list = new ArrayList<>();
                            if (response != null && response.getData() != null) {
                                for (StaffItemResponse staff : response.getData()) {
                                    if (staff.getStaffId() != null) {
                                        handledRequestManager.markHandled(staff.getStaffId());
                                    }

                                    String code = staff.getStaffId() != null && staff.getStaffId().length() >= 6
                                            ? "NV" + staff.getStaffId().substring(0, 4).toUpperCase()
                                            : "NV001";

                                    EmployeeStatus status = "deactive".equalsIgnoreCase(staff.getStatus())
                                            ? EmployeeStatus.TERMINATED
                                            : EmployeeStatus.ACTIVE;

                                    String pos = staff.getPosition() != null && !staff.getPosition().isEmpty()
                                            ? staff.getPosition()
                                            : "Nhân viên";

                                    String dob = staff.getDateOfBirth() != null ? staff.getDateOfBirth() : "";
                                    String avatar = staff.getAvatar();
                                    if (avatar != null && (avatar.equalsIgnoreCase("null") || avatar.equalsIgnoreCase("none"))) {
                                        avatar = null;
                                    }
                                    if (avatar != null && (avatar.startsWith("file://") || avatar.startsWith("/"))) {
                                        try {
                                            String path = avatar.startsWith("file://") ? android.net.Uri.parse(avatar).getPath() : avatar;
                                            java.io.File f = new java.io.File(path);
                                            if (!f.exists() || !f.canRead()) {
                                                avatar = null;
                                            }
                                        } catch (Exception ignored) {
                                            avatar = null;
                                        }
                                    }
                                    if ((avatar == null || avatar.trim().isEmpty()) && handledRequestManager != null) {
                                        for (ManagerEmployeeItem emp : handledRequestManager.getApprovedEmployees()) {
                                            if (emp.getUserId() != null && emp.getUserId().equals(staff.getStaffId())) {
                                                avatar = emp.getAvatarUrl();
                                                break;
                                            }
                                        }
                                    }
                                    if ((avatar == null || avatar.trim().isEmpty()) && appContext != null) {
                                        avatar = com.example.attendanceapp.data.local.StaffAvatarCache.getAvatarByName(appContext, staff.getNameStaff());
                                    }

                                    list.add(new ManagerEmployeeItem(
                                            staff.getStaffId(),
                                            code,
                                            staff.getNameStaff() != null ? staff.getNameStaff() : "Nhân viên",
                                            avatar != null ? avatar : "",
                                            pos,
                                            "Phòng ban chung",
                                            "",
                                            dob,
                                            status,
                                            0,
                                            0,
                                            0
                                    ));
                                }
                            }
                            _employees.setValue(list);
                            filterOutExistingEmployeesFromJoinRequests();
                        },
                        throwable -> {
                            if (handledRequestManager != null) {
                                _employees.setValue(handledRequestManager.getApprovedEmployees());
                            }
                        }
                ));
    }

    /**
     * Quản lý phê duyệt hoặc từ chối đơn gia nhập kèm phân bổ chức vụ
     */
    public void handleJoinRequest(String staffId, String fullName, String avatar, String position, boolean status, Runnable onSuccess) {
        if (staffId == null || staffId.trim().isEmpty()) return;
        if (processingStaffIds.contains(staffId)) return;
        processingStaffIds.add(staffId);

        String finalPosition = (status && position != null && !position.trim().isEmpty()) ? position.trim() : "";
        disposables.add(managerRepository.handleJoinRequest(new HandleJoinRequest(staffId, finalPosition, status))
                .doFinally(() -> processingStaffIds.remove(staffId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            // 1. Cập nhật UI ngay lập tức
                            removeStaffFromJoinRequests(staffId);
                            handledRequestManager.markHandled(staffId);

                            // 2. Nếu duyệt thành công, lưu vào danh sách nhân viên của công ty với đúng chức vụ được giao
                            if (status) {
                                String displayPos = !finalPosition.isEmpty() ? finalPosition : "Nhân viên";
                                handledRequestManager.addApprovedEmployee(staffId, fullName, avatar, displayPos);
                            }

                            // 3. Xóa thông báo khỏi Room DB
                            disposables.add(notificationRepository.deleteNotificationsByStaffId(staffId)
                                     .subscribeOn(Schedulers.io())
                                     .subscribe(() -> {}, err -> {}));

                            // 4. Tải lại danh sách nhân sự và yêu cầu
                            loadStaffs();
                            loadJoinRequests();

                            if (onSuccess != null) onSuccess.run();
                        },
                        throwable -> {
                            String msg = throwable.getMessage();
                            boolean isInCompany = msg != null && (msg.toLowerCase().contains("staff is in company") || msg.toLowerCase().contains("in company"));
                            if (throwable instanceof com.example.attendanceapp.domain.exception.AppException) {
                                int code = ((com.example.attendanceapp.domain.exception.AppException) throwable).getHttpCode();
                                if (code == 401 || code == 409) isInCompany = true;
                            }
                            if (isInCompany) {
                                // Backend báo nhân viên này đã ở trong công ty -> Xem như thành công
                                removeStaffFromJoinRequests(staffId);
                                handledRequestManager.markHandled(staffId);

                                if (status) {
                                    String displayPos = !finalPosition.isEmpty() ? finalPosition : "Nhân viên";
                                    handledRequestManager.addApprovedEmployee(staffId, fullName, avatar, displayPos);
                                }

                                disposables.add(notificationRepository.deleteNotificationsByStaffId(staffId)
                                         .subscribeOn(Schedulers.io())
                                         .subscribe(() -> {}, err -> {}));

                                loadStaffs();
                                loadJoinRequests();

                                if (onSuccess != null) onSuccess.run();
                            } else {
                                _errorMessage.setValue("Xử lý yêu cầu thất bại: " + throwable.getMessage());
                            }
                        }
                ));
    }

    public void handleJoinRequest(String staffId, String fullName, String avatar, boolean status, Runnable onSuccess) {
        handleJoinRequest(staffId, fullName, avatar, "Nhân viên", status, onSuccess);
    }

    public void handleJoinRequest(String staffId, boolean status, Runnable onSuccess) {
        handleJoinRequest(staffId, "Nhân viên mới", null, status, onSuccess);
    }

    public HandledRequestManager getHandledRequestManager() {
        return handledRequestManager;
    }

    /**
     * Tạo mã mời mới từ server và đồng bộ vào Room Session
     * @param expiresInSeconds Thời hạn hiệu lực tính bằng giây (ví dụ 86400 = 24 giờ)
     */
    public void generateInvitationCode(int expiresInSeconds) {
        _isGeneratingCode.setValue(true);
        _errorMessage.setValue(null);

        int validExpires = (expiresInSeconds > 0 && expiresInSeconds < 86400) ? expiresInSeconds : 86399;
        disposables.add(managerRepository.generateInvitationCode(validExpires)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            _isGeneratingCode.setValue(false);
                            if (response.getOtp() != null && !response.getOtp().isEmpty()) {
                                _invitationCode.setValue(response.getOtp());
                            }
                            _generateCodeSuccess.setValue(response);
                        },
                        throwable -> {
                            _isGeneratingCode.setValue(false);
                            _errorMessage.setValue(throwable.getMessage() != null ? throwable.getMessage() : "Không thể tạo mã mời");
                        }
                ));
    }

    public void setEmployeeSearchQuery(String query) {
        _employeeSearchQuery.setValue(query);
    }

    public void setDepartmentSearchQuery(String query) {
        _departmentSearchQuery.setValue(query);
    }

    public void setPositionSearchQuery(String query) {
        _positionSearchQuery.setValue(query);
    }

    public void setJoinRequestCount(int count) {
        _joinRequestCount.setValue(count);
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
