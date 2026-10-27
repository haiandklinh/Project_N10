package com.example.attendanceapp.data.repository;

import android.content.Context;

import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.data.dto.request.staff.AttendanceAdjustmentRequest;
import com.example.attendanceapp.data.dto.request.staff.ChangeShiftRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateLeaveRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateStaffProfileRequest;
import com.example.attendanceapp.data.dto.request.staff.JoinCompanyRequest;
import com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.staff.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffCompanyResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffProfileResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.StaffAvatarCache;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.ProfileErrors;
import com.example.attendanceapp.data.remote.StaffApi;
import com.example.attendanceapp.domain.model.ManagerInfo;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;

@Singleton
public class StaffRepositoryImpl implements StaffRepository {

    private final StaffApi api;
    private final SessionRepository session;
    private final AppStateRepository appState;
    private final WebSocketNotificationManager webSocketManager;
    private final Context context;

    @Inject
    public StaffRepositoryImpl(
            StaffApi api,
            SessionRepository session,
            AppStateRepository appState,
            WebSocketNotificationManager webSocketManager,
            @ApplicationContext Context context
    ) {
        this.api = api;
        this.session = session;
        this.appState = appState;
        this.webSocketManager = webSocketManager;
        this.context = context;
    }

    // ===== Profile =====

    @Override
    public Single<UserAccount> getProfile() {
        return api.getStaffProfile()
                .subscribeOn(Schedulers.io())
                .flatMap(response -> session.saveStaffProfile(response)
                        .doOnSuccess(account -> {
                            appState.saveUserRole(UserRole.EMPLOYEE);
                            if (account.getProfile() != null && account.getProfile().getStatus() != null) {
                                appState.saveUserStatus(account.getProfile().getStatus());
                            } else {
                                appState.saveUserStatus(UserStatus.ACTIVE);
                            }
                        }))
                .onErrorResumeNext(ProfileErrors::mapNotFound)
                .compose(ApiErrorMapper.forSingle("Lấy hồ sơ Nhân viên thất bại"));
    }

    @Override
    public Single<UserAccount> createProfile(CreateStaffProfileRequest request) {
        return api.createStaffProfile(request)
                .subscribeOn(Schedulers.io())
                .flatMap(session::saveStaffProfile)
                .doOnSuccess(account -> {
                    appState.saveUserRole(UserRole.EMPLOYEE);
                    if (account.getProfile() != null && account.getProfile().getStatus() != null) {
                        appState.saveUserStatus(account.getProfile().getStatus());
                    }
                })
                .compose(ApiErrorMapper.forSingle("Tạo hồ sơ Nhân viên thất bại"));
    }

    @Override
    public Single<UserAccount> updateProfile(UpdateStaffProfileRequest request) {
        return api.updateStaffProfile(request)
                .subscribeOn(Schedulers.io())
                .flatMap(session::saveStaffProfile)
                .compose(ApiErrorMapper.forSingle("Cập nhật hồ sơ Nhân viên thất bại"));
    }

    // ===== Gia nhập công ty =====

    @Override
    public Single<ManagerInfo> joinWithInvitationCode(String invitationCode, String name, String avatar) {
        String safeName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Nhân viên";
        String safeAvatar = avatar != null ? avatar : "";
        String code = invitationCode != null ? invitationCode.trim() : "";

        // Chuyển đổi avatar sang Base64 thumbnail để Quản lý trên các thiết bị khác hiển thị chuẩn ảnh của Nhân viên
        String base64Avatar = StaffAvatarCache.encodeImageToBase64(context, safeAvatar);
        if (base64Avatar != null && !base64Avatar.isEmpty()) {
            safeAvatar = base64Avatar;
        }

        JoinCompanyRequest body = new JoinCompanyRequest(code, safeName, safeAvatar);

        return api.sendJoinRequest(body)
                .subscribeOn(Schedulers.io())
                .flatMap(resp -> {
                    JSONObject json = readJson(resp);
                    String companyName = json.optString("company", json.optString("company_name", "Công ty"));
                    String managerId = json.optString("id_manager", "");
                    ManagerInfo info = new ManagerInfo(managerId, companyName, companyName, null);

                    return session.savePendingJoinRequest(code, companyName)
                            .andThen(session.saveInvitationCode(code))
                            .toSingleDefault(info);
                })
                .doOnSuccess(ignored -> {
                    appState.saveUserStatus(UserStatus.PENDING_APPROVAL);
                    webSocketManager.notifyJoinCompanyRequest(safeName);
                })
                .compose(ApiErrorMapper.forSingle("Gia nhập công ty thất bại"));
    }

    @Override
    public Single<Boolean> checkCompanyApprovalStatus(String invitationCode) {
        // 1. Ưu tiên kiểm tra qua GET /staffs/my-company
        // Đây là nguồn dữ liệu chuẩn xác nhất: Khi Quản lý duyệt đơn, nhân viên sẽ lập tức có trong danh sách my-company
        return api.getMyCompanies()
                .subscribeOn(Schedulers.io())
                .flatMap(response -> {
                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                        StaffCompanyResponse firstCompany = response.getData().get(0);
                        String managerId = firstCompany.getManagerId();
                        String companyName = firstCompany.getCompanyName();
                        String position = firstCompany.getPositionStaff();

                        return session.saveSelectedCompany(managerId, companyName, position)
                                .doOnComplete(() -> appState.saveUserStatus(UserStatus.APPROVED))
                                .toSingleDefault(true);
                    }

                    // 2. Nếu my-company chưa có, kiểm tra tiếp qua invitation code nếu có
                    if (invitationCode != null && !invitationCode.trim().isEmpty()) {
                        return checkStatusViaInvitationCode(invitationCode.trim());
                    }

                    appState.saveUserStatus(UserStatus.PENDING_APPROVAL);
                    return Single.just(false);
                })
                .onErrorResumeNext(error -> {
                    // Nếu getMyCompanies gặp lỗi, fallback kiểm tra qua invitation code
                    if (invitationCode != null && !invitationCode.trim().isEmpty()) {
                        return checkStatusViaInvitationCode(invitationCode.trim());
                    }
                    appState.saveUserStatus(UserStatus.PENDING_APPROVAL);
                    return Single.just(false);
                });
    }

    private Single<Boolean> checkStatusViaInvitationCode(String invitationCode) {
        return api.getManagerIdByInvitationCode(invitationCode.trim())
                .subscribeOn(Schedulers.io())
                .map(StaffRepositoryImpl::extractManagerId)
                .flatMap(managerId -> {
                    if (managerId != null && !managerId.isEmpty()) {
                        return session.saveApprovalSuccess(managerId)
                                .doOnComplete(() -> appState.saveUserStatus(UserStatus.APPROVED))
                                .toSingleDefault(true);
                    } else {
                        appState.saveUserStatus(UserStatus.PENDING_APPROVAL);
                        return Single.just(false);
                    }
                })
                .onErrorResumeNext(throwable -> {
                    appState.saveUserStatus(UserStatus.PENDING_APPROVAL);
                    return Single.just(false);
                });
    }

    private static String extractManagerId(ResponseBody body) throws Exception {
        JSONObject json = readJson(body);
        String id = json.optString("request_id", json.optString("id", json.optString("id_manager", "")));
        if (!id.isEmpty()) {
            return id;
        }
        String content = body.string().replace("\"", "").trim();
        if (!content.isEmpty()) {
            return content;
        }
        throw new IllegalStateException("Không tìm thấy id quản lý trong phản hồi");
    }

    private static ManagerInfo toManagerInfo(ResponseBody body) throws Exception {
        JSONObject json = readJson(body);
        String companyName = json.optString("company", json.optString("company_name", ""));
        String managerId = json.optString("request_id", json.optString("id", json.optString("id_manager", "")));
        String managerName = json.optString("manager_name", json.optString("name", companyName));

        return new ManagerInfo(
                managerId,
                managerName,
                companyName,
                null
        );
    }

    // ===== Nghiệp vụ khác =====

    @Override
    public Single<BaseListResponse<StaffCompanyResponse>> getMyCompanies() {
        return api.getMyCompanies()
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách công ty thất bại"));
    }

    @Override
    public Single<BaseListResponse<StaffWorkScheduleResponse>> getWorkSchedules() {
        return session.getCurrentUser()
                .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                .defaultIfEmpty("")
                .<BaseListResponse<StaffWorkScheduleResponse>>flatMap(managerId -> {
                    if (managerId.isEmpty()) {
                        return Single.just(new BaseListResponse<StaffWorkScheduleResponse>(new java.util.ArrayList<>()));
                    }
                    return api.getWorkSchedules(managerId);
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy lịch làm việc thất bại"));
    }

    @Override
    public Single<BaseListResponse<NotificationItemResponse>> getNotifications() {
        return session.getCurrentUser()
                .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                .defaultIfEmpty("")
                .<BaseListResponse<NotificationItemResponse>>flatMap(managerId -> {
                    if (managerId.isEmpty()) {
                        return Single.just(new BaseListResponse<NotificationItemResponse>(new java.util.ArrayList<>()));
                    }
                    return api.getNotifications(managerId);
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy thông báo thất bại"));
    }

    @Override
    public Completable requestChangeShift(ChangeShiftRequest request) {
        return session.getCurrentUser()
                .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                .defaultIfEmpty("")
                .flatMapCompletable(managerId -> {
                    if (managerId.isEmpty()) {
                        return Completable.error(new IllegalStateException("Chưa tham gia công ty"));
                    }
                    return api.requestChangeShift(managerId, request)
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess(ignored -> {
                                String reason = request != null ? request.getReason() : "";
                                String workday = request != null ? request.getWorkDay() : "";
                                webSocketManager.notifyShiftChangeRequest("Bạn", workday, reason);
                            })
                            .ignoreElement();
                })
                .compose(ApiErrorMapper.forCompletable("Gửi yêu cầu đổi ca thất bại"));
    }

    @Override
    public Completable requestLeave(CreateLeaveRequest request) {
        return session.getCurrentUser()
                .map(user -> user.getCompanyId() != null ? user.getCompanyId() : "")
                .defaultIfEmpty("")
                .flatMapCompletable(managerId -> {
                    if (managerId.isEmpty()) {
                        return Completable.error(new IllegalStateException("Chưa tham gia công ty"));
                    }
                    return api.requestLeave(managerId, request)
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess(ignored -> {
                                String reason = request != null ? request.getReason() : "";
                                String workday = request != null ? request.getWorkDayOff() : "";
                                webSocketManager.notifyLeaveRequest("Bạn", workday, reason);
                            })
                            .ignoreElement();
                })
                .compose(ApiErrorMapper.forCompletable("Gửi yêu cầu nghỉ phép thất bại"));
    }

    @Override
    public Completable requestAttendanceAdjustment(String managerId, AttendanceAdjustmentRequest request) {
        return api.requestAttendanceAdjustment(managerId, request)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(ignored -> {
                    String reason = request != null ? request.getReason() : "";
                    String workday = request != null ? request.getWorkday() : "";
                    webSocketManager.notifyAttendanceAdjustment("Bạn", workday, reason);
                })
                .ignoreElement()
                .compose(ApiErrorMapper.forCompletable("Gửi yêu cầu điều chỉnh chấm công thất bại"));
    }

    private static JSONObject readJson(ResponseBody body) {
        try {
            return new JSONObject(body.string());
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
