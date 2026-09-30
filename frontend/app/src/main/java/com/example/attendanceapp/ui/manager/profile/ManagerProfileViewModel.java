package com.example.attendanceapp.ui.manager.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.usecase.auth.LogoutUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ManagerProfileViewModel extends ViewModel {

    private final com.example.attendanceapp.domain.repository.UserRepository userRepository;
    private final LogoutUseCase logoutUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<com.example.attendanceapp.domain.model.UserAccount> userAccount = new MutableLiveData<>();

    @Inject
    public ManagerProfileViewModel(com.example.attendanceapp.domain.repository.UserRepository userRepository, LogoutUseCase logoutUseCase) {
        this.userRepository = userRepository;
        this.logoutUseCase = logoutUseCase;
        observeUser();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }
    public LiveData<com.example.attendanceapp.domain.model.UserAccount> getUserAccount() { return userAccount; }

    private void observeUser() {
        disposables.add(userRepository.observeCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userAccount::setValue,
                        throwable -> {
                            // Handle error
                        }
                ));
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
