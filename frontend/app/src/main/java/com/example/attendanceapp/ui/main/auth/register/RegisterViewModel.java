package com.example.attendanceapp.ui.main.auth.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
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

    /** Kiểm tra SĐT đã đăng ký chưa trước khi gửi OTP */
    public void checkPhoneAndSendOtp(String phone) {
        if (phone == null || phone.length() < 9) {
            otpState.setValue(new OtpState.Error("Số điện thoại không hợp lệ!"));
            return;
        }

        isLoading.setValue(true);

        registerUseCase.checkPhoneExists(phone, new AuthCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isExists) {
                if (isExists) {
                    isLoading.setValue(false);
                    otpState.setValue(new OtpState.Error("Số điện thoại này đã được đăng ký!"));
                } else {
                    sendOtpInternal(phone);
                }
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                otpState.setValue(new OtpState.Error(error));
            }
        });
    }

    /** Hàm gửi OTP thực tế sau khi đã kiểm tra trùng */
    private void sendOtpInternal(String phone) {
        registerUseCase.sendOtp(phone, new AuthCallback<Void>() {
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

    /** Xác thực OTP */
    public void verifyOtp(String phone, String otp) {
        isLoading.setValue(true);
        // Đã sửa AuthCallback<Void> thành AuthCallback<String>
        registerUseCase.verifyOtp(phone, otp, new AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
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

    /** Đăng ký tài khoản */
    public void register(String phone, String password) {
        isLoading.setValue(true);

        registerUseCase.execute(phone, password, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                registerUseCase.saveAccountSaved();
                isLoading.setValue(false);
                registerState.setValue(new RegisterState.Success());
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
            // Không còn chứa UserAccount vì API /register không trả về thông tin user,
            // chỉ trả message thành công. Sau khi Success, điều hướng người dùng
            // về màn hình Login để họ tự đăng nhập lại (đúng theo mô tả API gốc).
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