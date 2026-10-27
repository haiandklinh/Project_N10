package com.example.attendanceapp.ui.main.common.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.repository.NotificationRepository;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class NotificationViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<Notification>> _notifications = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    public LiveData<List<Notification>> getNotifications() {
        return _notifications;
    }

    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    @Inject
    public NotificationViewModel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        observeNotifications();
        refresh();
    }

    private void observeNotifications() {
        disposables.add(notificationRepository.observeNotifications()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        list -> _notifications.setValue(list != null ? list : Collections.emptyList()),
                        throwable -> _notifications.setValue(Collections.emptyList())
                ));
    }

    public void refresh() {
        _isLoading.setValue(true);
        disposables.add(notificationRepository.refreshNotifications()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> _isLoading.setValue(false))
                .subscribe(
                        () -> {},
                        throwable -> {}
                ));
    }

    public void markAsRead(String notificationId) {
        if (notificationId == null) return;
        disposables.add(notificationRepository.markAsRead(notificationId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {},
                        throwable -> {}
                ));
    }

    public void markAllAsRead() {
        disposables.add(notificationRepository.markAllAsRead()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {},
                        throwable -> {}
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
