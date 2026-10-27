package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.exception.AppException;
import com.example.attendanceapp.domain.model.PendingProfileSetup;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.StaffRepository;
import com.example.attendanceapp.domain.usecase.auth.CreateProfileUseCase;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;
import com.example.attendanceapp.domain.usecase.profile.LoadProfileAfterRoleUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final CreateProfileUseCase createProfileUseCase;
    private final LoadProfileAfterRoleUseCase loadProfileAfterRoleUseCase;
    private final StaffRepository staffRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<RoleSetupEvent> roleSetupEvent = new MutableLiveData<>();

    // State lưu giữ tiến trình setup role/profile
    private final PendingProfileSetup pendingProfile = new PendingProfileSetup();

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase,
                          CreateProfileUseCase createProfileUseCase,
                          LoadProfileAfterRoleUseCase loadProfileAfterRoleUseCase,
                          StaffRepository staffRepository) {
        this.loginUseCase = loginUseCase;
        this.createProfileUseCase = createProfileUseCase;
        this.loadProfileAfterRoleUseCase = loadProfileAfterRoleUseCase;
        this.staffRepository = staffRepository;
    }

    public LiveData<LoginState> getLoginState() { return loginState; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<RoleSetupEvent> getRoleSetupEvent() { return roleSetupEvent; }

    public void login(String phone, String password) {
        isLoading.setValue(true);
        disposables.add(loginUseCase.execute(phone, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            isLoading.setValue(false);
                            loginState.setValue(new LoginState.Success(result));
                        },
                        error -> {
                            isLoading.setValue(false);

                            if (error instanceof com.example.attendanceapp.domain.exception.AppException) {
                                com.example.attendanceapp.domain.exception.AppException appEx =
                                        (com.example.attendanceapp.domain.exception.AppException) error;

                                if (appEx.getHttpCode() == 404 || "USER_NOT_FOUND".equals(appEx.getErrorCode())) {
                                    loginState.setValue(new LoginState.PhoneNotRegistered(phone));
                                    return;
                                }
                            }

                            String message = error.getMessage();
                            disposables.add(loginUseCase.checkPhoneExists(phone)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            exists -> {
                                                if (!exists) {
                                                    loginState.setValue(new LoginState.PhoneNotRegistered(phone));
                                                } else {
                                                    loginState.setValue(new LoginState.Error(message != null ? message : "Số điện thoại hoặc mật khẩu không chính xác"));
                                                }
                                            },
                                            throwable -> loginState.setValue(new LoginState.Error(message))
                                    ));
                        }
                ));
    }

    public void checkPhoneRegistration(String phone) {
        isLoading.setValue(true);
        disposables.add(loginUseCase.checkPhoneExists(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        exists -> {
                            isLoading.setValue(false);
                            if (exists) {
                                loginState.setValue(new LoginState.Error("Vui lòng nhập mật khẩu!"));
                            } else {
                                loginState.setValue(new LoginState.PhoneNotRegistered(phone));
                            }
                        },
                        error -> {
                            isLoading.setValue(false);
                            loginState.setValue(new LoginState.Error(error.getMessage()));
                        }
                ));
    }

    // --- CÁC HÀM XỬ LÝ LUỒNG TỪ DIALOG & BOTTOM SHEET ---

    public void onRoleSelected(UserRole role) {
        pendingProfile.setRole(role);
        isLoading.setValue(true);

        disposables.add(loadProfileAfterRoleUseCase.execute(role)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result.isFound()) {
                                UserAccount user = result.getAccount();
                                if (role == UserRole.MANAGER) {
                                    isLoading.setValue(false);
                                    roleSetupEvent.setValue(new RoleSetupEvent.NavigateToHome(UserRole.MANAGER));
                                } else {
                                    handleEmployeeRouting(user);
                                }
                            } else {
                                isLoading.setValue(false);
                                roleSetupEvent.setValue(new RoleSetupEvent.ShowPersonalInfoForm());
                            }
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            roleSetupEvent.setValue(new RoleSetupEvent.ShowPersonalInfoForm());
                        }
                ));
    }

    private void handleEmployeeRouting(UserAccount user) {
        String inviteCode = (user != null && user.getInvitationCode() != null) ? user.getInvitationCode() : "";
        String companyName = (user != null && user.getCompanyName() != null) ? user.getCompanyName() : "";

        // Kiểm tra danh sách công ty đã tham gia từ API /staffs/my-company
        disposables.add(staffRepository.getMyCompanies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            isLoading.setValue(false);
                            if (res != null && res.getData() != null && !res.getData().isEmpty()) {
                                // Đã tham gia công ty -> Vào màn hình chọn công ty trước khi vào app
                                roleSetupEvent.setValue(new RoleSetupEvent.NavigateToCompanySelection());
                            } else if (!inviteCode.isEmpty()) {
                                roleSetupEvent.setValue(new RoleSetupEvent.NavigateToPendingApproval(inviteCode, companyName));
                            } else {
                                // Chưa có công ty nào -> Mở màn hình chọn công ty (hiển thị trạng thái rỗng + nút nhập mã mời)
                                roleSetupEvent.setValue(new RoleSetupEvent.NavigateToCompanySelection());
                            }
                        },
                        error -> {
                            isLoading.setValue(false);
                            roleSetupEvent.setValue(new RoleSetupEvent.NavigateToCompanySelection());
                        }
                ));
    }

    public void onPersonalInfoEntered(String name, String dob, String gender, String avatarUri) {
        pendingProfile.updatePersonalInfo(name, dob, gender, avatarUri);

        if (pendingProfile.getRole() == UserRole.MANAGER) {
            roleSetupEvent.setValue(new RoleSetupEvent.ShowCompanyNameForm());
        } else if (pendingProfile.getRole() == UserRole.EMPLOYEE) {
            roleSetupEvent.setValue(new RoleSetupEvent.ShowInvitationCodeForm());
        }
    }

    /** Tạo hồ sơ Quản lý kèm Tên công ty */
    public void submitManagerCompany(String companyName) {
        isLoading.setValue(true);

        disposables.add(createProfileUseCase.createManagerProfile(
                        pendingProfile.getFullName(),
                        pendingProfile.getDob(),
                        pendingProfile.getGender(),
                        pendingProfile.getAvatarUri(),
                        companyName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isLoading.setValue(false);
                            roleSetupEvent.setValue(new RoleSetupEvent.NavigateToHome(UserRole.MANAGER));
                        },
                        throwable -> {
                            if (isConflictError(throwable)) {
                                isLoading.setValue(false);
                                roleSetupEvent.setValue(new RoleSetupEvent.NavigateToHome(UserRole.MANAGER));
                            } else {
                                handleError(throwable.getMessage());
                            }
                        }
                ));
    }

    /** Tạo hồ sơ Nhân viên và Gửi yêu cầu gia nhập qua mã mời */
    public void submitEmployeeInvitationCode(String invitationCode) {
        isLoading.setValue(true);

        disposables.add(createProfileUseCase.createStaffProfile(
                        pendingProfile.getFullName(),
                        pendingProfile.getDob(),
                        pendingProfile.getGender(),
                        pendingProfile.getAvatarUri())
                .onErrorResumeNext(throwable -> {
                    if (isConflictError(throwable)) {
                        return staffRepository.updateProfile(new com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest(
                                pendingProfile.getFullName(),
                                pendingProfile.getGender(),
                                pendingProfile.getAvatarUri(),
                                pendingProfile.getDob()
                        )).ignoreElement();
                    }
                    return io.reactivex.rxjava3.core.Completable.error(throwable);
                })
                .andThen(createProfileUseCase.joinCompanyWithCode(
                        invitationCode,
                        pendingProfile.getFullName(),
                        pendingProfile.getAvatarUri()
                ))
                .flatMap(companyName -> staffRepository.checkCompanyApprovalStatus(invitationCode)
                        .map(isApproved -> new Object[]{companyName, isApproved}))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            isLoading.setValue(false);
                            String companyName = (String) res[0];
                            boolean isApproved = (Boolean) res[1];
                            if (isApproved) {
                                roleSetupEvent.setValue(new RoleSetupEvent.NavigateToCompanySelection());
                            } else {
                                roleSetupEvent.setValue(new RoleSetupEvent.NavigateToPendingApproval(invitationCode, companyName));
                            }
                        },
                        throwable -> handleError(throwable.getMessage())
                ));
    }

    private boolean isConflictError(Throwable throwable) {
        if (throwable instanceof AppException) {
            AppException ex = (AppException) throwable;
            return ex.getHttpCode() == 409 || (ex.getMessage() != null && ex.getMessage().contains("Hồ sơ đã được tạo"));
        }
        return false;
    }

    private void handleError(String errorMessage) {
        isLoading.setValue(false);
        roleSetupEvent.setValue(new RoleSetupEvent.ShowError(errorMessage));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    // --- EVENT CLASS DÙNG CHO NAVIGATION VÀ POPUP ---
    public static abstract class RoleSetupEvent {
        public static class ShowPersonalInfoForm extends RoleSetupEvent {}
        public static class ShowCompanyNameForm extends RoleSetupEvent {}
        public static class ShowInvitationCodeForm extends RoleSetupEvent {}
        public static class NavigateToCompanySelection extends RoleSetupEvent {}
        public static class ShowError extends RoleSetupEvent {
            public final String message;
            public ShowError(String message) { this.message = message; }
        }
        public static class NavigateToHome extends RoleSetupEvent {
            public final UserRole role;
            public NavigateToHome(UserRole role) { this.role = role; }
        }
        public static class NavigateToPendingApproval extends RoleSetupEvent {
            public final String inviteCode;
            public final String companyName;
            public NavigateToPendingApproval(String inviteCode, String companyName) {
                this.inviteCode = inviteCode;
                this.companyName = companyName;
            }
        }
    }
}
