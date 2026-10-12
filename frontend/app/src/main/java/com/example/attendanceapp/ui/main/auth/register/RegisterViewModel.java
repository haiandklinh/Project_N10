package com.example.attendanceapp.ui.main.auth.register;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.core.notification.MyFirebaseMessagingService;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.domain.usecase.auth.RegisterUseCase;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final RegisterUseCase registerUseCase;
    private final Context context;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<RegisterState> registerState = new MutableLiveData<>(RegisterState.Idle.INSTANCE);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<OtpState> otpState = new MutableLiveData<>(OtpState.Idle.INSTANCE);

    @Inject
    public RegisterViewModel(RegisterUseCase registerUseCase, @ApplicationContext Context context) {
        this.registerUseCase = registerUseCase;
        this.context = context;
    }

    public LiveData<RegisterState> getRegisterState() {
        return registerState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<OtpState> getOtpState() {
        return otpState;
    }

    /** Kiểm tra SĐT đã đăng ký chưa trước khi gửi OTP */
    public void checkPhoneAndSendOtp(String phone) {
        if (phone == null || phone.length() < 9) {
            otpState.setValue(new OtpState.Error("Số điện thoại không hợp lệ!"));
            return;
        }

        isLoading.setValue(true);
        otpState.setValue(OtpState.Loading.INSTANCE);

        disposables.add(registerUseCase.checkPhoneExists(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isExists -> {
                            if (isExists) {
                                isLoading.setValue(false);
                                otpState.setValue(new OtpState.Error("Số điện thoại này đã được đăng ký!"));
                            } else {
                                sendOtpInternal(phone);
                            }
                        },
                        error -> {
                            isLoading.setValue(false);
                            otpState.setValue(new OtpState.Error(error.getMessage()));
                        }
                ));
    }

    /** Hàm gửi OTP thực tế sau khi đã kiểm tra trùng */
    private void sendOtpInternal(String phone) {
        disposables.add(registerUseCase.sendOtp(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        otpCode -> {
                            isLoading.setValue(false);
                            // Hiển thị mã OTP lên thông báo của thiết bị bằng Model Notification
                            if (otpCode != null && !otpCode.isEmpty()) {
                                Notification notification = new Notification(
                                        UUID.randomUUID().toString(),
                                        "Mã xác thực OTP",
                                        "Mã đăng ký của bạn là: " + otpCode,
                                        LocalDateTime.now(),
                                        NotificationType.AUTH,
                                        false
                                );
                                MyFirebaseMessagingService.showStaticNotification(context, notification);
                            }
                            otpState.setValue(new OtpState.Sent(phone));
                        },
                        error -> {
                            isLoading.setValue(false);
                            otpState.setValue(new OtpState.Error(error.getMessage()));
                        }
                ));
    }

    /** Xác thực OTP */
    public void verifyOtp(String phone, String otp) {
        isLoading.setValue(true);
        otpState.setValue(OtpState.Loading.INSTANCE);

        disposables.add(registerUseCase.verifyOtp(phone, otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            isLoading.setValue(false);
                            otpState.setValue(OtpState.Verified.INSTANCE);
                        },
                        error -> {
                            isLoading.setValue(false);
                            otpState.setValue(new OtpState.Error(error.getMessage()));
                        }
                ));
    }

    /** Đăng ký tài khoản */
    public void register(String phone, String password) {
        isLoading.setValue(true);
        registerState.setValue(RegisterState.Loading.INSTANCE);

        disposables.add(registerUseCase.execute(phone, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            registerState.setValue(new RegisterState.Success());
                        },
                        error -> {
                            isLoading.setValue(false);
                            registerState.setValue(new RegisterState.Error(error.getMessage()));
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
