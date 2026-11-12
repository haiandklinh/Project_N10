package com.example.attendanceapp.domain.usecase;

import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.domain.model.SplashDestination;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import javax.inject.Inject;

// Kiểm tra trạng thái hiện tại của ứng dụng trong bộ nhớ
// (đã đăng nhập chưa, đã lưu tài khoản chưa, có phải lần đầu mở app không)
// để quyết định xem màn hình tiếp theo sau Splash Screen sẽ là màn hình nào (SplashDestination).
public class CheckAppStateUseCase {

    private final AppStateRepository repository;
    private final SessionRepository sessionRepository;
    private final TokenManager tokenManager;

    @Inject
    public CheckAppStateUseCase(AppStateRepository repository, 
                               SessionRepository sessionRepository,
                               TokenManager tokenManager) {
        this.repository = repository;
        this.sessionRepository = sessionRepository;
        this.tokenManager = tokenManager;
    }

    public SplashDestination execute() {
        boolean isLoggedIn = repository.isLoggedIn();
        boolean isAccountSaved = repository.isAccountSaved();

        boolean isFirstLaunch = repository.isFirstLaunch();

        android.util.Log.d("APP_STATE_DEBUG", "isLoggedIn: " + isLoggedIn
                + " | isAccountSaved: " + isAccountSaved
                + " | isFirstLaunch: " + isFirstLaunch
                + " | hasToken: " + (tokenManager != null && tokenManager.hasToken()));

        // Force clear nếu là lần đầu launch
        if (isFirstLaunch) {
            android.util.Log.d("APP_STATE_DEBUG", "Force clear session due to first launch");
            repository.clearSession();
            repository.setLoggedIn(false);
            repository.setAccountSaved(false);
            return SplashDestination.ONBOARDING;
        }

        // Force clear nếu isLoggedIn nhưng Room DB không có user hoặc mất JWT Token (sau khi restore/reinstall)
        if (isLoggedIn && (!hasValidSession() || !hasValidToken())) {
            android.util.Log.d("APP_STATE_DEBUG", "Force clear session - invalid session or missing token");
            repository.clearSession();
            repository.setLoggedIn(false);
            if (isAccountSaved) {
                return SplashDestination.LOGIN_SAVED_ACCOUNT;
            }
            return SplashDestination.ONBOARDING;
        }

        if (isLoggedIn) {
            UserRole role = repository.getUserRole();
            String status = repository.getUserStatus();

            // Nếu là Nhân viên chưa được duyệt vào công ty, luôn ở màn hình chờ phê duyệt
            if (role == UserRole.EMPLOYEE) {
                if (!"APPROVED".equalsIgnoreCase(status)) {
                    return SplashDestination.PENDING_APPROVAL;
                }
            }

            if ("PENDING_APPROVAL".equalsIgnoreCase(status)) {
                return SplashDestination.PENDING_APPROVAL;
            }
            return SplashDestination.HOME;
        }
        if (isAccountSaved) {
            return SplashDestination.LOGIN_SAVED_ACCOUNT;
        }

        // Nếu chưa từng login hoặc lưu tài khoản (chưa register/login) thì luôn hiển thị Onboarding
        return SplashDestination.ONBOARDING;
    }

    private boolean hasValidSession() {
        try {
            return sessionRepository.getSavedAccountData().blockingGet() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasValidToken() {
        return tokenManager != null && tokenManager.hasToken();
    }
}
