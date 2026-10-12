package com.example.attendanceapp.ui.manager.approval;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ManagerApprovalViewModel extends ViewModel {

    private final MutableLiveData<Integer> _pendingApprovalCount = new MutableLiveData<>(0);
    public LiveData<Integer> pendingApprovalCount = _pendingApprovalCount;

    @Inject
    public ManagerApprovalViewModel() {
        // Khởi tạo badge count bằng 0, sẽ được cập nhật từ Fragment/Repository
        _pendingApprovalCount.setValue(0);
    }

    public void setPendingApprovalCount(int count) {
        _pendingApprovalCount.setValue(count);
    }
}
