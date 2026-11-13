package com.example.attendanceapp.data.repository;

import android.content.Context;
import com.example.attendanceapp.core.utils.CloudImageUploader;
import com.example.attendanceapp.data.local.StaffAvatarCache;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.data.dto.request.manager.CalculatePayrollRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateBonusRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateDeductionRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleJoinRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleLeaveRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleShiftChangeRequest;
import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.CalculatePayrollResponse;
import com.example.attendanceapp.data.dto.response.manager.CompanyInvitationResponse;
import com.example.attendanceapp.data.dto.response.manager.CreateBonusResponse;
import com.example.attendanceapp.data.dto.response.manager.CreateDeductionResponse;
import com.example.attendanceapp.data.dto.response.manager.ManagerProfileResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffPayrollSummaryResponse;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.ManagerApi;
import com.example.attendanceapp.data.remote.ProfileErrors;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class ManagerRepositoryImpl implements ManagerRepository {

    private final ManagerApi api;
    private final SessionRepository session;
    private final AppStateRepository appState;
    private final WebSocketNotificationManager webSocketManager;
    private final Context context;

    @Inject
    public ManagerRepositoryImpl(
            ManagerApi api,
            SessionRepository session,
            AppStateRepository appState,
            WebSocketNotificationManager webSocketManager,
            @ApplicationContext Context context) {
        this.api = api;
        this.session = session;
        this.appState = appState;
        this.webSocketManager = webSocketManager;
        this.context = context;
    }

    // ===== Profile =====

    @Override
    public Single<UserAccount> getProfile() {
        return api.getManagerProfile()
                .subscribeOn(Schedulers.io())
                .map(response -> {
                    if (!isPlaceholderAvatar(response.getAvatar()) && context != null) {
                        StaffAvatarCache.saveAvatar(context, response.getFullName(), null, response.getAvatar());
                    }
                    return response;
                })
                .flatMap(response -> session.saveManagerProfile(response)
                        .doOnSuccess(account -> {
                            appState.saveUserRole(UserRole.MANAGER);
                            if (account.getProfile() != null && account.getProfile().getStatus() != null) {
                                appState.saveUserStatus(account.getProfile().getStatus());
                            } else {
                                appState.saveUserStatus(UserStatus.ACTIVE);
                            }
                        }))
                .onErrorResumeNext(ProfileErrors::mapNotFound)
                .compose(ApiErrorMapper.forSingle("Lấy hồ sơ Quản lý thất bại"));
    }

    @Override
    public Single<UserAccount> createProfile(CreateManagerProfileRequest request) {
        String avatar = request.getAvatar();
        if (avatar != null && !avatar.isEmpty() && context != null) {
            String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatar);
            String finalAvatar = (cloudUrl != null) ? cloudUrl : "";
            request.setAvatar(finalAvatar);
        }
        return api.createManagerProfile(request)
                .subscribeOn(Schedulers.io())
                .map(response -> {
                    if (response.getFullName() == null || response.getFullName().isEmpty()) {
                        response.setFullName(request.getFullName());
                    }
                    if (response.getCompany() == null || response.getCompany().isEmpty()) {
                        response.setCompany(request.getCompany());
                    }
                    if (isPlaceholderAvatar(response.getAvatar())) {
                        response.setAvatar(isPlaceholderAvatar(request.getAvatar()) ? null : request.getAvatar());
                    }
                    if (!isPlaceholderAvatar(response.getAvatar()) && context != null) {
                        StaffAvatarCache.saveAvatar(context, response.getFullName(), null, response.getAvatar());
                    }
                    return response;
                })
                .flatMap(session::saveManagerProfile)
                .doOnSuccess(account -> {
                    appState.saveUserRole(UserRole.MANAGER);
                    appState.saveUserStatus(UserStatus.ACTIVE); // Hoặc UserStatus.COMPLETED
                })
                .compose(ApiErrorMapper.forSingle("Tạo hồ sơ Quản lý thất bại"));
    }

    @Override
    public Single<UserAccount> updateProfile(UpdateManagerProfileRequest request) {
        String avatar = request.getAvatar();
        if (!isPlaceholderAvatar(avatar) && context != null) {
            String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatar);
            String finalAvatar = (cloudUrl != null) ? cloudUrl : "";
            request.setAvatar(finalAvatar);
        }
        return api.updateManagerProfile(request)
                .subscribeOn(Schedulers.io())
                .map(response -> {
                    if (response.getFullName() == null || response.getFullName().isEmpty()) {
                        response.setFullName(request.getFullName());
                    }
                    if (response.getCompany() == null || response.getCompany().isEmpty()) {
                        response.setCompany(request.getCompany());
                    }
                    if (isPlaceholderAvatar(response.getAvatar())) {
                        response.setAvatar(isPlaceholderAvatar(request.getAvatar()) ? null : request.getAvatar());
                    }
                    if (!isPlaceholderAvatar(response.getAvatar()) && context != null) {
                        StaffAvatarCache.saveAvatar(context, response.getFullName(), null, response.getAvatar());
                    }
                    return response;
                })
                .flatMap(session::saveManagerProfile)
                .doOnSuccess(account -> {
                    // Update user in local AppState
                    appState.saveUserRole(UserRole.MANAGER);
                    if (account.getProfile() != null && account.getProfile().getStatus() != null) {
                        appState.saveUserStatus(account.getProfile().getStatus());
                    }
                })
                .compose(ApiErrorMapper.forSingle("Cập nhật hồ sơ Quản lý thất bại"));
    }

    private boolean isPlaceholderAvatar(String avatar) {
        if (avatar == null) return true;
        String trimmed = avatar.trim();
        return trimmed.isEmpty()
                || "null".equalsIgnoreCase(trimmed)
                || "none".equalsIgnoreCase(trimmed)
                || "string".equalsIgnoreCase(trimmed);
    }

    // ===== Invitation Code =====

    @Override
    public Single<String> getInvitationCode() {
        return session.getCachedInvitationCode()
                .switchIfEmpty(
                        // Gọi server với TTL mặc định 86399s (~24h, hợp lệ chuẩn time 00:00:00 - 23:59:59)
                        api.getInvitationCode(86399)
                                .subscribeOn(Schedulers.io())
                                .map(CompanyInvitationResponse::getOtp)
                                .flatMap(code -> session.saveInvitationCode(code).toSingleDefault(code))
                )
                .compose(ApiErrorMapper.forSingle("Lấy mã mời thất bại"));
    }

    @Override
    public Single<CompanyInvitationResponse> generateInvitationCode(int expiresIn) {
        int validExpires = (expiresIn > 0) ? Math.min(expiresIn, 86399) : 86399;
        return api.getInvitationCode(validExpires)
                .subscribeOn(Schedulers.io())
                .flatMap(response -> {
                    if (response != null && response.getOtp() != null) {
                        return session.saveInvitationCode(response.getOtp()).toSingleDefault(response);
                    }
                    return Single.just(response);
                })
                .compose(ApiErrorMapper.forSingle("Tạo mã mời thất bại"));
    }

    // ===== Notifications & Actions =====

    @Override
    public Single<BaseListResponse<NotificationItemResponse>> getNotifications() {
        return api.getNotifications()
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách thông báo thất bại"));
    }

    @Override
    public Single<BaseListResponse<StaffItemResponse>> getStaffs() {
        return api.getStaffs()
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách nhân viên thất bại"));
    }

    @Override
    public Completable handleShiftChange(HandleShiftChangeRequest request) {
        return api.handleShiftChange(request)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(ignored -> {
                    boolean approved = request != null && request.isStatus();
                    String reason = request != null ? request.getReason() : "";
                    webSocketManager.notifyApprovalResult("Đổi ca làm việc", approved, reason);
                })
                .ignoreElement()
                .compose(ApiErrorMapper.forCompletable("Xử lý yêu cầu đổi ca thất bại"));
    }

    @Override
    public Completable handleLeaveRequest(HandleLeaveRequest request) {
        return api.handleLeaveRequest(request)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(ignored -> {
                    boolean approved = request != null && request.isStatus();
                    String reason = request != null ? request.getReason() : "";
                    webSocketManager.notifyApprovalResult("Đơn xin nghỉ phép", approved, reason);
                })
                .ignoreElement()
                .compose(ApiErrorMapper.forCompletable("Xử lý yêu cầu nghỉ phép thất bại"));
    }

    @Override
    public Completable handleJoinRequest(HandleJoinRequest request) {
        return api.handleJoinRequest(request)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(ignored -> {
                    boolean approved = request != null && request.isStatus();
                    webSocketManager.notifyApprovalResult("Yêu cầu tham gia công ty", approved, "");
                })
                .ignoreElement()
                .compose(ApiErrorMapper.forCompletable("Xử lý yêu cầu tham gia thất bại"))
                .onErrorResumeNext(throwable -> {
                    String msg = throwable.getMessage();
                    boolean isInCompany = msg != null && (msg.toLowerCase().contains("staff is in company") || msg.toLowerCase().contains("in company"));
                    if (throwable instanceof com.example.attendanceapp.domain.exception.AppException) {
                        int code = ((com.example.attendanceapp.domain.exception.AppException) throwable).getHttpCode();
                        if (code == 401 || code == 409) isInCompany = true;
                    }
                    if (isInCompany) {
                        boolean approved = request != null && request.isStatus();
                        webSocketManager.notifyApprovalResult("Yêu cầu tham gia công ty", approved, "");
                        return Completable.complete();
                    }
                    return Completable.error(throwable);
                });
    }

    @Override
    public Single<okhttp3.ResponseBody> getApprovalRequests() {
        return api.getApprovalRequests()
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách yêu cầu duyệt thất bại"));
    }

    // ===== Nghiệp vụ lương thưởng nhân sự =====

    @Override
    public Single<StaffPayrollSummaryResponse> getStaffPayrollSummary(String staffId) {
        return api.getStaffPayrollSummary(staffId)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy thông tin tổng hợp lương thất bại"));
    }

    @Override
    public Single<CalculatePayrollResponse> calculatePayroll(CalculatePayrollRequest request) {
        return api.calculatePayroll(request)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Tính lương nhân viên thất bại"));
    }

    @Override
    public Single<BaseListResponse<CalculatePayrollResponse>> getPayrollHistory(String staffId) {
        return api.getPayrollHistory(staffId)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy lịch sử tính lương thất bại"));
    }

    @Override
    public Single<CreateBonusResponse> createBonus(CreateBonusRequest request) {
        return api.createBonus(request)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Tạo khen thưởng thất bại"));
    }

    @Override
    public Single<CreateDeductionResponse> createDeduction(CreateDeductionRequest request) {
        return api.createDeduction(request)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Tạo khấu trừ thất bại"));
    }

}
