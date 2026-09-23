package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;
import com.example.attendanceapp.domain.usecase.auth.RoleSelectionUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;
    private final RoleSelectionUseCase roleSelectionUseCase;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase, RoleSelectionUseCase roleSelectionUseCase) {
        this.loginUseCase = loginUseCase;
        this.roleSelectionUseCase = roleSelectionUseCase;
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String phone, String password) {
        isLoading.setValue(true);
        loginUseCase.execute(phone, password, new AuthCallback<UserAccount>() {
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

    public void handleManagerRoleSelection(String companyName, AuthCallback<Void> callback) {
        isLoading.setValue(true);
        roleSelectionUseCase.selectRole(UserRole.MANAGER, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                roleSelectionUseCase.createCompany(companyName, new AuthCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        isLoading.setValue(false);
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        isLoading.setValue(false);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                callback.onError(errorMessage);
            }
        });
    }

    public void handleEmployeeRoleSelection(String invitationCode, AuthCallback<String> callback) {
        isLoading.setValue(true);
        roleSelectionUseCase.selectRole(UserRole.EMPLOYEE, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                roleSelectionUseCase.joinCompanyWithCode(invitationCode, new AuthCallback<String>() {
                    @Override
                    public void onSuccess(String companyName) {
                        isLoading.setValue(false);
                        callback.onSuccess(companyName);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        isLoading.setValue(false);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                callback.onError(errorMessage);
            }
        });
    }
}