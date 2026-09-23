package com.example.attendanceapp.ui.main.auth.forgotpassword;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
import com.example.attendanceapp.domain.usecase.auth.ForgotPasswordUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ForgotPasswordViewModel extends ViewModel {

    private final ForgotPasswordUseCase forgotPasswordUseCase;

    private final MutableLiveData<ForgotPasswordState> forgotPasswordState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private String currentAccount = "";

    @Inject
    public ForgotPasswordViewModel(ForgotPasswordUseCase forgotPasswordUseCase) {
        this.forgotPasswordUseCase = forgotPasswordUseCase;
    }

    public LiveData<ForgotPasswordState> getForgotPasswordState() {
        return forgotPasswordState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void sendOtp(String account) {
        if (account == null || account.trim().isEmpty()) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Vui lòng nhập số điện thoại hoặc email"));
            return;
        }

        currentAccount = account.trim();
        isLoading.setValue(true);

        forgotPasswordUseCase.sendOtp(currentAccount, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.OtpSent(currentAccount));
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.Error(error));
            }
        });
    }

    public void verifyOtp(String otp) {
        if (otp == null || otp.length() < 6) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Vui lòng nhập mã OTP 6 số"));
            return;
        }

        isLoading.setValue(true);

        forgotPasswordUseCase.verifyOtp(currentAccount, otp, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.OtpVerified());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.Error(error));
            }
        });
    }

    public void resetPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Vui lòng nhập mật khẩu mới"));
            return;
        }

        if (newPassword.length() < 6) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Mật khẩu phải có ít nhất 6 ký tự"));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Mật khẩu xác nhận không khớp"));
            return;
        }

        isLoading.setValue(true);

        forgotPasswordUseCase.resetPassword(currentAccount, newPassword, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.Success());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.Error(error));
            }
        });
    }

    public void resendOtp() {
        isLoading.setValue(true);

        forgotPasswordUseCase.sendOtp(currentAccount, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.OtpResent());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                forgotPasswordState.setValue(new ForgotPasswordState.Error(error));
            }
        });
    }

    // --- Sealed/State Classes ---

    public static abstract class ForgotPasswordState {
        public static class OtpSent extends ForgotPasswordState {
            public final String account;
            public OtpSent(String account) {
                this.account = account;
            }
        }

        public static class OtpVerified extends ForgotPasswordState {}

        public static class OtpResent extends ForgotPasswordState {}

        public static class Success extends ForgotPasswordState {}

        public static class Error extends ForgotPasswordState {
            public final String message;
            public Error(String message) {
                this.message = message;
            }
        }
    }
}
