package com.example.attendanceapp.ui.main.auth.company;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.data.dto.response.staff.StaffCompanyResponse;
import com.example.attendanceapp.domain.model.ManagerInfo;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;
import com.example.attendanceapp.domain.usecase.auth.LogoutUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class StaffCompanySelectionViewModel extends ViewModel {

    private final StaffRepository staffRepository;
    private final SessionRepository sessionRepository;
    private final AppStateRepository appStateRepository;
    private final LogoutUseCase logoutUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<StaffCompanyResponse>> _companies = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<StaffCompanyResponse>> companies = _companies;

    // Trạng thái kéo để làm mới danh sách
    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    public LiveData<Boolean> isRefreshing = _isRefreshing;
    public LiveData<Boolean> isLoading = _isRefreshing; // Giữ tương thích ngược

    // Trạng thái xử lý hành động (chọn công ty, gửi mã mời, đăng xuất)
    private final MutableLiveData<Boolean> _isActionLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isActionLoading = _isActionLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<StaffCompanyResponse> _companySelectedEvent = new MutableLiveData<>(null);
    public LiveData<StaffCompanyResponse> companySelectedEvent = _companySelectedEvent;

    private final MutableLiveData<ManagerInfo> _joinSuccessEvent = new MutableLiveData<>(null);
    public LiveData<ManagerInfo> joinSuccessEvent = _joinSuccessEvent;

    private final MutableLiveData<Boolean> _logoutSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> logoutSuccess = _logoutSuccess;

    private final MutableLiveData<String> _userName = new MutableLiveData<>("");
    public LiveData<String> userName = _userName;

    private String lastSubmittedCode = "";

    @Inject
    public StaffCompanySelectionViewModel(
            StaffRepository staffRepository,
            SessionRepository sessionRepository,
            AppStateRepository appStateRepository,
            LogoutUseCase logoutUseCase
    ) {
        this.staffRepository = staffRepository;
        this.sessionRepository = sessionRepository;
        this.appStateRepository = appStateRepository;
        this.logoutUseCase = logoutUseCase;

        loadUserProfile();
        loadCompanies();
    }

    public String getLastSubmittedCode() {
        return lastSubmittedCode;
    }

    private void loadUserProfile() {
        disposables.add(sessionRepository.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (user != null) {
                                String name = user.getFullName() != null && !user.getFullName().trim().isEmpty()
                                        ? user.getFullName()
                                        : "Bạn";
                                _userName.setValue(name);
                            }
                        },
                        throwable -> {}
                ));
    }

    public void loadCompanies() {
        _isRefreshing.setValue(true);
        disposables.add(staffRepository.getMyCompanies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            _isRefreshing.setValue(false);
                            if (response != null && response.getData() != null) {
                                _companies.setValue(response.getData());
                            } else {
                                _companies.setValue(new ArrayList<>());
                            }
                        },
                        throwable -> {
                            _isRefreshing.setValue(false);
                            _errorMessage.setValue(throwable.getMessage());
                            _companies.setValue(new ArrayList<>());
                        }
                ));
    }

    public void selectCompany(StaffCompanyResponse company) {
        // Chống bấm liên tiếp nhiều lần khi đang xử lý
        if (Boolean.TRUE.equals(_isActionLoading.getValue())) {
            return;
        }

        if (company == null || company.getManagerId() == null || company.getManagerId().trim().isEmpty()) {
            _errorMessage.setValue("Không tìm thấy thông tin quản lý của doanh nghiệp");
            return;
        }

        _isActionLoading.setValue(true);
        String managerId = company.getManagerId().trim();
        String companyName = company.getCompanyName() != null ? company.getCompanyName() : "Doanh nghiệp";
        String position = company.getPositionStaff() != null ? company.getPositionStaff() : "Nhân viên";

        disposables.add(sessionRepository.saveSelectedCompany(managerId, companyName, position)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            _isActionLoading.setValue(false);
                            appStateRepository.setLoggedIn(true);
                            appStateRepository.saveUserStatus(UserStatus.APPROVED);
                            _companySelectedEvent.setValue(company);
                        },
                        throwable -> {
                            _isActionLoading.setValue(false);
                            _errorMessage.setValue("Không thể lưu thông tin công ty: " + throwable.getMessage());
                        }
                ));
    }

    public void joinCompanyWithCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            _errorMessage.setValue("Vui lòng nhập mã mời hợp lệ");
            return;
        }

        if (Boolean.TRUE.equals(_isActionLoading.getValue())) {
            return;
        }

        _isActionLoading.setValue(true);
        String safeCode = code.trim();
        lastSubmittedCode = safeCode;

        disposables.add(sessionRepository.getCurrentUser()
                .map(user -> new String[]{
                        user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName() : "Nhân viên",
                        user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
                })
                .defaultIfEmpty(new String[]{"Nhân viên", ""})
                .flatMap(info -> staffRepository.joinWithInvitationCode(safeCode, info[0], info[1]))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        managerInfo -> {
                            _isActionLoading.setValue(false);
                            _joinSuccessEvent.setValue(managerInfo);
                        },
                        throwable -> {
                            _isActionLoading.setValue(false);
                            _errorMessage.setValue("Lỗi gia nhập: " + throwable.getMessage());
                        }
                ));
    }

    public void consumeCompanySelectedEvent() {
        _companySelectedEvent.setValue(null);
    }

    public void consumeJoinSuccessEvent() {
        _joinSuccessEvent.setValue(null);
    }

    public void logout() {
        if (Boolean.TRUE.equals(_isActionLoading.getValue())) {
            return;
        }

        _isActionLoading.setValue(true);
        disposables.add(logoutUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            _isActionLoading.setValue(false);
                            _logoutSuccess.setValue(true);
                        },
                        throwable -> {
                            _isActionLoading.setValue(false);
                            // Dù lỗi mạng vẫn xác nhận đăng xuất cục bộ an toàn
                            _logoutSuccess.setValue(true);
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
