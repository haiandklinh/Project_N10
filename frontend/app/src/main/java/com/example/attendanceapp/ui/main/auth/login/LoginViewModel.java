package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String phone, String password) {
        isLoading.setValue(true);
        loginUseCase.execute(phone, password, new LoginUseCase.AuthCallback<UserAccount>() {
            @Override
            public void onSuccess(UserAccount result) {
                loginUseCase.setLoggedIn(true);
                loginUseCase.setAccountSaved(true);
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