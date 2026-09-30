package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.usecase.auth.GetSavedAccountUseCase;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class LoginSavedAccountViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;
    private final GetSavedAccountUseCase getSavedAccountUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<SavedAccountData> savedAccountData = new MutableLiveData<>();

    @Inject
    public LoginSavedAccountViewModel(LoginUseCase loginUseCase, GetSavedAccountUseCase getSavedAccountUseCase) {
        this.loginUseCase = loginUseCase;
        this.getSavedAccountUseCase = getSavedAccountUseCase;
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
        SavedAccountData data = getSavedAccountUseCase.execute();
        savedAccountData.setValue(data);
        // Fragment sẽ xử lý khi data == null bằng cách chuyển về Login thường
    }

    public void loginWithPassword(String password) {
        isLoading.setValue(true);

        SavedAccountData saved = savedAccountData.getValue();
        if (saved == null) {
            isLoading.setValue(false);
            loginState.setValue(new LoginState.Error("Không tìm thấy tài khoản đã lưu"));
            return;
        }

        disposables.add(loginUseCase.execute(saved.phone, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            isLoading.setValue(false);
                            loginState.setValue(new LoginState.Success(result));
                        },
                        error -> {
                            isLoading.setValue(false);
                            loginState.setValue(new LoginState.Error(error.getMessage()));
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
