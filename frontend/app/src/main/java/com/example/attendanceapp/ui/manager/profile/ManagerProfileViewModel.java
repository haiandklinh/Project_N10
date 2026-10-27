package com.example.attendanceapp.ui.manager.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.usecase.auth.LogoutUseCase;
import com.example.attendanceapp.domain.usecase.profile.GetProfileUseCase;
import com.example.attendanceapp.domain.usecase.profile.ObserveCurrentUserUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerProfileViewModel extends ViewModel {

    private final ObserveCurrentUserUseCase observeCurrentUserUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final ManagerRepository managerRepository;
    private final LogoutUseCase logoutUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<UserAccount> userAccount = new MutableLiveData<>();

    @Inject
    public ManagerProfileViewModel(
            ObserveCurrentUserUseCase observeCurrentUserUseCase,
            GetProfileUseCase getProfileUseCase,
            ManagerRepository managerRepository,
            LogoutUseCase logoutUseCase
    ) {
        this.observeCurrentUserUseCase = observeCurrentUserUseCase;
        this.getProfileUseCase = getProfileUseCase;
        this.managerRepository = managerRepository;
        this.logoutUseCase = logoutUseCase;

        observeUser();
        loadProfile();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }
    public LiveData<UserAccount> getUserAccount() { return userAccount; }

    private void observeUser() {
        disposables.add(observeCurrentUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userAccount::setValue,
                        throwable -> {
                            // Bỏ qua: giữ dữ liệu hiện có
                        }
                ));
    }

    public void loadProfile() {
        // Đồng bộ hồ sơ mới nhất từ server/mock vào Room
        disposables.add(getProfileUseCase.execute()
                .subscribeOn(Schedulers.io())
                .subscribe(profile -> { }, throwable -> { }));

        // Đồng bộ mã mời từ server/mock vào Session/Room
        disposables.add(managerRepository.getInvitationCode()
                .subscribeOn(Schedulers.io())
                .subscribe(code -> { }, throwable -> { }));
    }

    public void logout() {
        isLoading.setValue(true);
        disposables.add(logoutUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            logoutSuccess.setValue(true);
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            logoutSuccess.setValue(true);
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
