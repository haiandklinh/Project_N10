package com.example.attendanceapp.ui.manager.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.profile.GetCurrentUserUseCase;
import com.example.attendanceapp.domain.usecase.profile.UpdateProfileUseCase;

import java.time.LocalDate;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerEditBasicInfoViewModel extends ViewModel {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<UserAccount> userAccount = new MutableLiveData<>();

    @Inject
    public ManagerEditBasicInfoViewModel(UpdateProfileUseCase updateProfileUseCase,
                                         GetCurrentUserUseCase getCurrentUserUseCase) {
        this.updateProfileUseCase = updateProfileUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        loadUserData();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<UserAccount> getUserAccount() { return userAccount; }

    private void loadUserData() {
        disposables.add(getCurrentUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userAccount::setValue,
                        throwable -> error.setValue(throwable.getMessage()),
                        () -> error.setValue("Chưa có thông tin tài khoản")
                ));
    }

    public void updateProfile(String fullName, LocalDate dob, Gender gender, Uri avatarUri, String companyName) {
        isLoading.setValue(true);
        disposables.add(updateProfileUseCase.execute(fullName, dob, gender, avatarUri, UserRole.MANAGER, companyName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            updateSuccess.setValue(true);
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            error.setValue(throwable.getMessage());
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
