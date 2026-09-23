package com.example.attendanceapp.ui.main.auth.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.usecase.auth.RegisterUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final RegisterUseCase registerUseCase;
    private final MutableLiveData<RegisterState> registerState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<OtpState> otpState = new MutableLiveData<>();

    @Inject
    public RegisterViewModel(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
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

    /** Gọi API gửi OTP đến số điện thoại */
    public void sendOtp(String phone) {
        if (phone == null || phone.length() < 10) {
            otpState.setValue(new OtpState.Error("Số điện thoại không hợp lệ"));
            return;
        }

        isLoading.setValue(true);
        // Gọi UseCase/Repository gửi OTP thực tế (hoặc qua callback)
        registerUseCase.sendOtp(phone, new RegisterUseCase.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                otpState.setValue(new OtpState.Sent(phone));
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                otpState.setValue(new OtpState.Error(error));
            }
        });
    }

    /** Xác thực OTP giả lập/thực tế */
    public void verifyOtp(String phone, String otp) {
        isLoading.setValue(true);
        registerUseCase.verifyOtp(phone, otp, new RegisterUseCase.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                otpState.setValue(new OtpState.Verified());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                otpState.setValue(new OtpState.Error(error));
            }
        });
    }

    /** Đăng ký tài khoản - Chỉ còn Họ tên, SĐT, Mật khẩu */
    public void register(String fullName, String phone, String password) {
        isLoading.setValue(true);

        // Gọi UseCase đăng ký (Đã bỏ tham số role)
        registerUseCase.execute(fullName, phone, password,
                new RegisterUseCase.AuthCallback<UserAccount>() {
                    @Override
                    public void onSuccess(UserAccount result) {
                        registerUseCase.saveAccountSaved();
                        isLoading.setValue(false);
                        registerState.setValue(new RegisterState.Success(result));
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.setValue(false);
                        registerState.setValue(new RegisterState.Error(error));
                    }
                });
    }

    // --- Sealed/State Classes ---

    public static abstract class RegisterState {
        public static class Success extends RegisterState {
            public final UserAccount user;
            public Success(UserAccount user) {
                this.user = user;
            }
        }

        public static class Error extends RegisterState {
            public final String message;
            public Error(String message) {
                this.message = message;
            }
        }
    }

    public static abstract class OtpState {
        public static class Sent extends OtpState {
            public final String phone;
            public Sent(String phone) {
                this.phone = phone;
            }
        }

        public static class Verified extends OtpState {}

        public static class Error extends OtpState {
            public final String message;
            public Error(String message) {
                this.message = message;
            }
        }
    }
}