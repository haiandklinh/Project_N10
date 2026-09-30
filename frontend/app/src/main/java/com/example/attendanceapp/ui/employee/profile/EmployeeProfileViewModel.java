package com.example.attendanceapp.ui.employee.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.UserRepository;
import com.example.attendanceapp.domain.usecase.auth.LogoutUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class EmployeeProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final LogoutUseCase logoutUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<UserAccount> userAccount = new MutableLiveData<>();

    @Inject
    public EmployeeProfileViewModel(UserRepository userRepository, LogoutUseCase logoutUseCase) {
        this.userRepository = userRepository;
        this.logoutUseCase = logoutUseCase;
        observeUser();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }
    public LiveData<UserAccount> getUserAccount() { return userAccount; }

    private void observeUser() {
        disposables.add(userRepository.observeCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userAccount::setValue,
                        throwable -> {
                            // Handle error if needed
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
