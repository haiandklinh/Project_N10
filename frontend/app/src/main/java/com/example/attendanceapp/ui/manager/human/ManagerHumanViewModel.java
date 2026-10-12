package com.example.attendanceapp.ui.manager.human;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ManagerHumanViewModel extends ViewModel {

    private final MutableLiveData<String> _employeeSearchQuery = new MutableLiveData<>("");
    public LiveData<String> employeeSearchQuery = _employeeSearchQuery;

    private final MutableLiveData<String> _departmentSearchQuery = new MutableLiveData<>("");
    public LiveData<String> departmentSearchQuery = _departmentSearchQuery;

    private final MutableLiveData<String> _positionSearchQuery = new MutableLiveData<>("");
    public LiveData<String> positionSearchQuery = _positionSearchQuery;

    private final MutableLiveData<Integer> _joinRequestCount = new MutableLiveData<>(0);
    public LiveData<Integer> joinRequestCount = _joinRequestCount;

    @Inject
    public ManagerHumanViewModel() {
        // Khởi tạo badge count bằng 0, sẽ được cập nhật từ Fragment/Repository
        _joinRequestCount.setValue(0);
    }

    public void setEmployeeSearchQuery(String query) {
        _employeeSearchQuery.setValue(query);
    }

    public void setDepartmentSearchQuery(String query) {
        _departmentSearchQuery.setValue(query);
    }

    public void setPositionSearchQuery(String query) {
        _positionSearchQuery.setValue(query);
    }

    public void setJoinRequestCount(int count) {
        _joinRequestCount.setValue(count);
    }
}