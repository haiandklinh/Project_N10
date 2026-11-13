package com.example.attendanceapp.ui.common.password;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.usecase.auth.ChangePasswordUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ChangePasswordViewModel extends ViewModel {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<ChangePasswordState> state = new MutableLiveData<>();

    @Inject
    public ChangePasswordViewModel(ChangePasswordUseCase changePasswordUseCase) {
        this.changePasswordUseCase = changePasswordUseCase;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<ChangePasswordState> getState() {
        return state;
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            state.setValue(new ChangePasswordState.Error("Vui lòng nhập mật khẩu hiện tại", ErrorField.CURRENT_PASSWORD));
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            state.setValue(new ChangePasswordState.Error("Vui lòng nhập mật khẩu mới", ErrorField.NEW_PASSWORD));
            return;
        }

        if (newPassword.length() < 6) {
            state.setValue(new ChangePasswordState.Error("Mật khẩu mới phải có ít nhất 6 ký tự", ErrorField.NEW_PASSWORD));
            return;
        }

        if (newPassword.equals(currentPassword)) {
            state.setValue(new ChangePasswordState.Error("Mật khẩu mới không được trùng với mật khẩu hiện tại", ErrorField.NEW_PASSWORD));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            state.setValue(new ChangePasswordState.Error("Mật khẩu xác nhận không khớp", ErrorField.CONFIRM_PASSWORD));
            return;
        }

        isLoading.setValue(true);
        disposables.add(changePasswordUseCase.execute(currentPassword, newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            state.setValue(new ChangePasswordState.Success());
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            String msg = throwable.getMessage() != null ? throwable.getMessage() : "Đổi mật khẩu thất bại";
                            if (msg.contains("hiện tại") || msg.contains("CURRENT_PASSWORD")) {
                                state.setValue(new ChangePasswordState.Error(msg, ErrorField.CURRENT_PASSWORD));
                            } else {
                                state.setValue(new ChangePasswordState.Error(msg, ErrorField.GENERAL));
                            }
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public enum ErrorField {
        CURRENT_PASSWORD,
        NEW_PASSWORD,
        CONFIRM_PASSWORD,
        GENERAL
    }

    public static abstract class ChangePasswordState {
        public static class Success extends ChangePasswordState {}
        public static class Error extends ChangePasswordState {
            public final String message;
            public final ErrorField field;

            public Error(String message, ErrorField field) {
                this.message = message;
                this.field = field;
            }
        }
    }
}
