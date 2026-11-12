package com.example.attendanceapp.ui.manager.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.profile.GetCurrentUserUseCase;
import com.example.attendanceapp.domain.usecase.profile.UpdateProfileUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerCompanySettingsViewModel extends ViewModel {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final SharedPreferenceApp sharedPreferenceApp;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<UserAccount> userAccount = new MutableLiveData<>();
    private final MutableLiveData<String> companyLocation = new MutableLiveData<>();

    @Inject
    public ManagerCompanySettingsViewModel(UpdateProfileUseCase updateProfileUseCase,
                                           GetCurrentUserUseCase getCurrentUserUseCase,
                                           SharedPreferenceApp sharedPreferenceApp) {
        this.updateProfileUseCase = updateProfileUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.sharedPreferenceApp = sharedPreferenceApp;
        loadUserData();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<UserAccount> getUserAccount() { return userAccount; }
    public LiveData<String> getCompanyLocation() { return companyLocation; }

    private void loadUserData() {
        disposables.add(getCurrentUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            userAccount.setValue(user);
                            companyLocation.setValue(sharedPreferenceApp.getCompanyAddress());
                        },
                        throwable -> error.setValue(throwable.getMessage())
                ));
    }

    public void updateBusinessInfo(String companyName, String location) {
        if (companyName == null || companyName.trim().isEmpty()) {
            error.setValue("business_settings_error_empty_name");
            return;
        }

        UserAccount current = userAccount.getValue();
        if (current == null) {
            error.setValue("business_settings_error_no_account");
            return;
        }

        isLoading.setValue(true);
        disposables.add(updateProfileUseCase.execute(
                        current.getFullName(),
                        current.getDateOfBirth(),
                        current.getGender(),
                        null, // Not changing avatar here
                        UserRole.MANAGER,
                        companyName.trim()
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            // Cập nhật location vào SharedPrefs nếu có
                            if (location != null && !location.trim().isEmpty()) {
                                sharedPreferenceApp.saveCompanyLocation(0, 0, location);
                            }
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
