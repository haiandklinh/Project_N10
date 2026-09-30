package com.example.attendanceapp.ui.manager.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.UserRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ManagerHomeViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<ManagerHomeUiState> _uiState = new MutableLiveData<>(ManagerHomeUiState.loading());

    public LiveData<ManagerHomeUiState> getUiState() {
        return _uiState;
    }

    @Inject
    public ManagerHomeViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        loadHomeData();
        observeUserAccount();
    }

    private void observeUserAccount() {
        disposables.add(userRepository.observeCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    ManagerHomeUiState current = _uiState.getValue();
                    if (current != null) {
                        _uiState.setValue(new ManagerHomeUiState(
                                user.getDisplayName(),
                                user.getCompanyName(),
                                user.getAvatarUrl()
                        ));
                    }
                }, throwable -> {
                    // Handle error if needed
                }));
    }

    public void loadHomeData() {
        disposables.add(userRepository.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    _uiState.setValue(new ManagerHomeUiState(
                            user.getDisplayName(),
                            user.getCompanyName(),
                            user.getAvatarUrl()
                    ));
                }, throwable -> {
                    _uiState.setValue(ManagerHomeUiState.error(throwable.getLocalizedMessage()));
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public static class ManagerHomeUiState {
        private final String userName;
        private final String companyName;
        private final String avatarUrl;
        private final boolean isLoading;
        private final String errorMessage;

        private ManagerHomeUiState(String userName, String companyName, String avatarUrl, boolean isLoading, String errorMessage) {
            this.userName = userName;
            this.companyName = companyName;
            this.avatarUrl = avatarUrl;
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
        }

        public static ManagerHomeUiState loading() {
            return new ManagerHomeUiState(null, null, null, true, null);
        }

        public static ManagerHomeUiState error(String errorMessage) {
            return new ManagerHomeUiState(null, null, null, false, errorMessage);
        }

        public ManagerHomeUiState(String userName, String companyName, String avatarUrl) {
            this(userName, companyName, avatarUrl, false, null);
        }

        public String getUserName() {
            return userName;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}