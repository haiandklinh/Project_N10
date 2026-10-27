package com.example.attendanceapp.data.repository;



import com.example.attendanceapp.core.notification.WebSocketNotificationManager;

import com.example.attendanceapp.data.dto.request.manager.CreateManagerProfileRequest;

import com.example.attendanceapp.data.dto.request.manager.HandleJoinRequest;

import com.example.attendanceapp.data.dto.request.manager.HandleLeaveRequest;

import com.example.attendanceapp.data.dto.request.manager.HandleShiftChangeRequest;

import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;

import com.example.attendanceapp.data.dto.response.BaseListResponse;

import com.example.attendanceapp.data.dto.response.manager.CompanyInvitationResponse;

import com.example.attendanceapp.data.dto.response.manager.ManagerProfileResponse;

import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;

import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;

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



    @Inject

    public ManagerRepositoryImpl(

            ManagerApi api,

            SessionRepository session,

            AppStateRepository appState,

            WebSocketNotificationManager webSocketManager

    ) {

        this.api = api;

        this.session = session;

        this.appState = appState;

        this.webSocketManager = webSocketManager;

    }



    // ===== Profile =====



    @Override

    public Single<UserAccount> getProfile() {

        return api.getManagerProfile()

                .subscribeOn(Schedulers.io())

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

        return api.createManagerProfile(request)

                .subscribeOn(Schedulers.io())

                .map(response -> {

                    if (response.getFullName() == null || response.getFullName().isEmpty()) {

                        response.setFullName(request.getFullName());

                    }

                    if (response.getCompany() == null || response.getCompany().isEmpty()) {

                        response.setCompany(request.getCompany());

                    }

                    if (response.getAvatar() == null || response.getAvatar().isEmpty()) {

                        response.setAvatar(request.getAvatar());

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

        return api.updateManagerProfile(request)

                .subscribeOn(Schedulers.io())

                .map(response -> {

                    if (response.getFullName() == null || response.getFullName().isEmpty()) {

                        response.setFullName(request.getFullName());

                    }

                    if (response.getCompany() == null || response.getCompany().isEmpty()) {

                        response.setCompany(request.getCompany());

                    }

                    if (response.getAvatar() == null || response.getAvatar().isEmpty()) {

                        response.setAvatar(request.getAvatar());

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



    // ===== Invitation Code =====



    @Override

    public Single<String> getInvitationCode() {

        return session.getCachedInvitationCode()

                .switchIfEmpty(

                        // Gọi server với TTL mặc định 86400s (24h)

                        api.getInvitationCode(86400)

                                .subscribeOn(Schedulers.io())

                                .map(CompanyInvitationResponse::getOtp)

                                .flatMap(code -> session.saveInvitationCode(code).toSingleDefault(code))

                )

                .compose(ApiErrorMapper.forSingle("Lấy mã mời thất bại"));

    }



    @Override

    public Single<CompanyInvitationResponse> generateInvitationCode(int expiresIn) {

        return api.getInvitationCode(expiresIn)

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



    @Override

    public Single<okhttp3.ResponseBody> getEmployees() {

        return api.getEmployees()

                .subscribeOn(Schedulers.io())

                .compose(ApiErrorMapper.forSingle("Lấy danh sách nhân viên thất bại"));

    }



    @Override

    public Single<okhttp3.ResponseBody> getPayrollDetail() {

        return api.getPayrollDetail()

                .subscribeOn(Schedulers.io())

                .compose(ApiErrorMapper.forSingle("Lấy chi tiết lương thất bại"));

    }

}

