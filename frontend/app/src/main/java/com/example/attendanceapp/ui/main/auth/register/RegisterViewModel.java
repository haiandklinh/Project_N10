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

    /** Goi API gui OTP den so dien thoai */
    public void sendOtp(String phone) {
        // TODO: goi API that su
        otpState.setValue(new OtpState.Sent(phone));
    }

    /** Xac thuc OTP - tam thoi gia lap dong bo de test UI */
    public boolean verifyOtpSync(String phone, String otp) {
        // TODO: goi API verify OTP that su
        return "123456".equals(otp);
    }

    /** Dang ky tai khoan - chi con SDT, khong con email/role/contact/isEmail */
    public void register(String fullName, String companyName, String phone, String password) {
        isLoading.setValue(true);
        registerUseCase.execute(fullName, companyName, phone, password,
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

    public static class RegisterState {
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

    public static class OtpState {
        public static class Sent extends OtpState {
            public final String phone;
            public Sent(String phone) {
                this.phone = phone;
            }
        }

        public static class Error extends OtpState {
            public final String message;
            public Error(String message) {
                this.message = message;
            }
        }
    }
}