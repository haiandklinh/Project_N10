package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class PendingApprovalViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;
    private final StaffRepository staffRepository;
    private final SessionRepository sessionRepository;
    private final WebSocketNotificationManager webSocketManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<ApprovalStatusState> approvalStatusState = new MutableLiveData<>();
    private final MutableLiveData<CompanyInfo> companyInfo = new MutableLiveData<>();

    private String cachedInviteCode = "";

    @Inject
    public PendingApprovalViewModel(
            AuthRepository authRepository,
            AppStateRepository appStateRepository,
            StaffRepository staffRepository,
            SessionRepository sessionRepository,
            WebSocketNotificationManager webSocketManager
    ) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
        this.staffRepository = staffRepository;
        this.sessionRepository = sessionRepository;
        this.webSocketManager = webSocketManager;

        // Kết nối WebSocket để lắng nghe realtime
        this.webSocketManager.connect();
        observeRealtimeWebSocket();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<ApprovalStatusState> getApprovalStatusState() {
        return approvalStatusState;
    }

    public LiveData<CompanyInfo> getCompanyInfo() {
        return companyInfo;
    }

    public void init(String inviteCode, String companyName) {
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            this.cachedInviteCode = inviteCode.trim();
            if (companyName != null && !companyName.trim().isEmpty() && !companyName.equals(this.cachedInviteCode)) {
                companyInfo.setValue(new CompanyInfo(this.cachedInviteCode, companyName));
            } else {
                fetchFreshCompanyName(this.cachedInviteCode);
            }
            checkApprovalStatus(this.cachedInviteCode);
        } else {
            // Đọc từ Session DB nếu không được truyền từ Bundle
            disposables.add(sessionRepository.getCurrentUser()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {
                                this.cachedInviteCode = user.getInvitationCode() != null ? user.getInvitationCode() : "";
                                String cName = user.getCompanyName() != null ? user.getCompanyName() : "";
                                if (!cName.isEmpty() && !cName.equals(this.cachedInviteCode)) {
                                    companyInfo.setValue(new CompanyInfo(this.cachedInviteCode, cName));
                                } else if (!this.cachedInviteCode.isEmpty()) {
                                    fetchFreshCompanyName(this.cachedInviteCode);
                                }
                                if (!this.cachedInviteCode.isEmpty()) {
                                    checkApprovalStatus(this.cachedInviteCode);
                                }
                            },
                            error -> {}
                    ));
        }
    }

    private void fetchFreshCompanyName(String code) {
        disposables.add(sessionRepository.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            String name = user.getCompanyName();
                            if (name != null && !name.isEmpty() && !name.equals(code)) {
                                companyInfo.setValue(new CompanyInfo(code, name));
                            } else {
                                companyInfo.setValue(new CompanyInfo(code, "Doanh nghiệp"));
                            }
                        },
                        throwable -> {
                            companyInfo.setValue(new CompanyInfo(code, "Doanh nghiệp"));
                        }
                ));
    }

    private void observeRealtimeWebSocket() {
        disposables.add(webSocketManager.observeJoinApprovalResult()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isApproved -> {
                            if (isApproved) {
                                checkApprovalStatus();
                            } else {
                                approvalStatusState.setValue(new ApprovalStatusState.Rejected("Quản lý đã từ chối yêu cầu gia nhập của bạn."));
                            }
                        },
                        throwable -> {}
                ));
    }

    /** Kiểm tra trạng thái phê duyệt qua StaffRepository */
    public void checkApprovalStatus() {
        checkApprovalStatus(cachedInviteCode);
    }

    public void checkApprovalStatus(String inviteCode) {
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            cachedInviteCode = inviteCode.trim();
        }
        isLoading.setValue(true);

        disposables.add(staffRepository.checkCompanyApprovalStatus(cachedInviteCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isApproved -> {
                            isLoading.setValue(false);
                            if (isApproved) {
                                appStateRepository.setLoggedIn(true);
                                appStateRepository.saveUserStatus(UserStatus.APPROVED);
                                approvalStatusState.setValue(new ApprovalStatusState.Approved());
                            } else {
                                approvalStatusState.setValue(new ApprovalStatusState.Pending());
                            }
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            approvalStatusState.setValue(new ApprovalStatusState.Pending());
                        }
                ));
    }

    /** Đăng xuất tài khoản sử dụng RxJava 3 Completable */
    public void logout() {
        isLoading.setValue(true);
        disposables.add(authRepository.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> isLoading.setValue(false),
                        throwable -> isLoading.setValue(false)
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    // --- Data Classes ---
    public static class CompanyInfo {
        public final String inviteCode;
        public final String companyName;
        public CompanyInfo(String inviteCode, String companyName) {
            this.inviteCode = inviteCode;
            this.companyName = companyName;
        }
    }

    // --- State Classes ---
    public static abstract class ApprovalStatusState {
        public static class Approved extends ApprovalStatusState {}
        public static class Pending extends ApprovalStatusState {}
        public static class Rejected extends ApprovalStatusState {
            public final String message;
            public Rejected(String message) { this.message = message; }
        }
        public static class Error extends ApprovalStatusState {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }
}
