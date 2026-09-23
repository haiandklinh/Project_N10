package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginSavedAccountViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<SavedAccountData> savedAccountData = new MutableLiveData<>();

    @Inject
    public LoginSavedAccountViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<SavedAccountData> getSavedAccountData() {
        return savedAccountData;
    }

    public void loadSavedAccount() {
        SavedAccountData data = loginUseCase.getSavedAccount();
        if (data != null) {
            savedAccountData.setValue(data);
        } else {
            loginState.setValue(new LoginState.Error("Không tìm thấy thông tin tài khoản đã lưu"));
        }
    }

    public void loginWithPassword(String password) {
        isLoading.setValue(true);

        SavedAccountData saved = savedAccountData.getValue();
        if (saved == null) {
            isLoading.setValue(false);
            loginState.setValue(new LoginState.Error("Không tìm thấy tài khoản đã lưu"));
            return;
        }

        loginUseCase.execute(saved.phone, password,
                new AuthCallback<UserAccount>() {
                    @Override
                    public void onSuccess(UserAccount result) {
                        loginUseCase.setLoggedIn(true);
                        isLoading.setValue(false);
                        loginState.setValue(new LoginState.Success(result));
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.setValue(false);
                        loginState.setValue(new LoginState.Error(error));
                    }
                });
    }
}