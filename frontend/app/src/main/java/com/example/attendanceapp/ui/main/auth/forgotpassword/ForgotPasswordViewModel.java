package com.example.attendanceapp.ui.main.auth.forgotpassword;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.usecase.auth.ForgotPasswordUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ForgotPasswordViewModel extends ViewModel {

    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<ForgotPasswordState> forgotPasswordState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private String currentAccount = "";
    private String resetToken = "";

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

        disposables.add(forgotPasswordUseCase.sendOtp(currentAccount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            forgotPasswordState.setValue(new ForgotPasswordState.OtpSent(currentAccount));
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            String message = throwable.getMessage();

                            // Kiểm tra nếu lỗi chỉ ra rằng tài khoản không tồn tại (Backend thật hoặc Mock)
                            if (throwable instanceof com.example.attendanceapp.domain.exception.AppException) {
                                com.example.attendanceapp.domain.exception.AppException appEx =
                                        (com.example.attendanceapp.domain.exception.AppException) throwable;
                                if (appEx.getHttpCode() == 404 || "USER_NOT_FOUND".equals(appEx.getErrorCode())) {
                                    forgotPasswordState.setValue(new ForgotPasswordState.PhoneNotRegistered(currentAccount));
                                    return;
                                }
                            }

                            // Fallback: Nếu backend trả về message chứa text đặc trưng hoặc ta muốn chắc chắn hơn
                            if (message != null && (message.contains("không tồn tại") || message.contains("chưa đăng ký") || message.contains("not found"))) {
                                forgotPasswordState.setValue(new ForgotPasswordState.PhoneNotRegistered(currentAccount));
                            } else {
                                forgotPasswordState.setValue(new ForgotPasswordState.Error(message));
                            }
                        }
                ));
    }

    public void verifyOtp(String otp) {
        if (otp == null || otp.length() < 6) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Vui lòng nhập mã OTP 6 số"));
            return;
        }

        isLoading.setValue(true);

        disposables.add(forgotPasswordUseCase.verifyOtp(currentAccount, otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            isLoading.setValue(false);
                            resetToken = result; // Lưu resetToken từ server
                            forgotPasswordState.setValue(new ForgotPasswordState.OtpVerified());
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            forgotPasswordState.setValue(new ForgotPasswordState.Error(throwable.getMessage()));
                        }
                ));
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

        if (resetToken == null || resetToken.isEmpty()) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Phiên xác thực đã hết hạn. Vui lòng gửi lại OTP"));
            return;
        }

        isLoading.setValue(true);

        disposables.add(forgotPasswordUseCase.resetPassword(resetToken, newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            resetToken = ""; // Clear resetToken sau khi dùng
                            forgotPasswordState.setValue(new ForgotPasswordState.Success());
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            forgotPasswordState.setValue(new ForgotPasswordState.Error(throwable.getMessage()));
                        }
                ));
    }

    public void resendOtp() {
        if (currentAccount.isEmpty()) {
            forgotPasswordState.setValue(new ForgotPasswordState.Error("Không tìm thấy thông tin tài khoản"));
            return;
        }

        isLoading.setValue(true);

        disposables.add(forgotPasswordUseCase.sendOtp(currentAccount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            forgotPasswordState.setValue(new ForgotPasswordState.OtpResent());
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            forgotPasswordState.setValue(new ForgotPasswordState.Error(throwable.getMessage()));
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    // --- State Classes ---

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

        public static class PhoneNotRegistered extends ForgotPasswordState {
            public final String phone;
            public PhoneNotRegistered(String phone) {
                this.phone = phone;
            }
        }

        public static class Error extends ForgotPasswordState {
            public final String message;
            public Error(String message) {
                this.message = message;
            }
        }
    }
}
