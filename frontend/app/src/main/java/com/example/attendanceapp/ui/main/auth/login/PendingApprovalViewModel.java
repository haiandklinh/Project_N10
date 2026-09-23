package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PendingApprovalViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<ApprovalStatusState> approvalStatusState = new MutableLiveData<>();

    @Inject
    public PendingApprovalViewModel(AuthRepository authRepository, AppStateRepository appStateRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<ApprovalStatusState> getApprovalStatusState() {
        return approvalStatusState;
    }

    /** Kiểm tra trạng thái phê duyệt qua Repository */
    public void checkApprovalStatus() {
        isLoading.setValue(true);

        // TODO: Khi có API thật, thay bằng hàm authRepository.checkApprovalStatus(...)
        // Hiện tại dùng delay giả lập API
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isLoading.setValue(false);

            // Mặc định false (chưa duyệt). Đổi sang true nếu giả lập được duyệt
            boolean isApproved = false;

            if (isApproved) {
                // Cập nhật trạng thái thành APPROVED trong SharedPreferences
                appStateRepository.setLoggedIn(true);
                approvalStatusState.setValue(new ApprovalStatusState.Approved());
            } else {
                approvalStatusState.setValue(new ApprovalStatusState.Pending());
            }
        }, 1500);
    }

    /** Đăng xuất tài khoản */
    public void logout() {
        authRepository.logout(new AuthRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // TODO: điều hướng về màn hình Login sau khi logout thành công
            }

            @Override
            public void onError(String errorMessage) {
                // Session local đã được clear trong AuthRepositoryImpl dù lỗi,
                // nên vẫn có thể điều hướng về Login ở đây nếu muốn
            }
        });
    }
    // --- State Classes ---
    public static abstract class ApprovalStatusState {
        public static class Approved extends ApprovalStatusState {}
        public static class Pending extends ApprovalStatusState {}
        public static class Error extends ApprovalStatusState {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }
}