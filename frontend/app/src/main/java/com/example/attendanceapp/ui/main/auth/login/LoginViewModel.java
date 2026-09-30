package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.PendingProfileSetup;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;
import com.example.attendanceapp.domain.usecase.auth.RoleSelectionUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final RoleSelectionUseCase roleSelectionUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<RoleSetupEvent> roleSetupEvent = new MutableLiveData<>();

    // State lưu giữ tiến trình setup role
    private final PendingProfileSetup pendingProfile = new PendingProfileSetup();

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase, RoleSelectionUseCase roleSelectionUseCase) {
        this.loginUseCase = loginUseCase;
        this.roleSelectionUseCase = roleSelectionUseCase;
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
                                
                                // Nếu Backend trả về 404 hoặc mã lỗi USER_NOT_FOUND
                                if (appEx.getHttpCode() == 404 || "USER_NOT_FOUND".equals(appEx.getErrorCode())) {
                                    loginState.setValue(new LoginState.PhoneNotRegistered(phone));
                                    return;
                                }
                            }

                            // Fallback: Nếu không phải 404, dùng checkPhoneExists để xác nhận lại cho chắc chắn
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
                                // SĐT đã tồn tại, yêu cầu nhập mật khẩu
                                loginState.setValue(new LoginState.Error("Vui lòng nhập mật khẩu!"));
                            } else {
                                // SĐT chưa tồn tại, hiện dialog chuyển sang đăng ký
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
        roleSetupEvent.setValue(new RoleSetupEvent.ShowPersonalInfoForm());
    }

    public void onPersonalInfoEntered(String name, String dob, String gender, String avatarUri) {
        pendingProfile.updatePersonalInfo(name, dob, gender, avatarUri);

        if (pendingProfile.getRole() == UserRole.MANAGER) {
            roleSetupEvent.setValue(new RoleSetupEvent.ShowCompanyNameForm());
        } else if (pendingProfile.getRole() == UserRole.EMPLOYEE) {
            roleSetupEvent.setValue(new RoleSetupEvent.ShowInvitationCodeForm());
        }
    }

    public void submitManagerCompany(String companyName) {
        isLoading.setValue(true);

        // Chaining các Completable và Single qua RxJava
        disposables.add(roleSelectionUseCase.updatePersonalInfo(
                pendingProfile.getFullName(),
                pendingProfile.getDob(),
                pendingProfile.getGender(),
                pendingProfile.getAvatarUri(),
                UserRole.MANAGER)
                .andThen(roleSelectionUseCase.selectRole(UserRole.MANAGER))
                .andThen(roleSelectionUseCase.createCompany(companyName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        invitationCode -> {
                            isLoading.setValue(false);
                            roleSetupEvent.setValue(new RoleSetupEvent.NavigateToHome(UserRole.MANAGER));
                        },
                        throwable -> handleError(throwable.getMessage())
                ));
    }

    public void submitEmployeeInvitationCode(String invitationCode) {
        isLoading.setValue(true);

        // Chaining các Completable và Single qua RxJava, truyền đủ 3 tham số
        disposables.add(roleSelectionUseCase.updatePersonalInfo(
                        pendingProfile.getFullName(),
                        pendingProfile.getDob(),
                        pendingProfile.getGender(),
                        pendingProfile.getAvatarUri(),
                        UserRole.EMPLOYEE)
                .andThen(roleSelectionUseCase.selectRole(UserRole.EMPLOYEE))
                .andThen(roleSelectionUseCase.joinCompanyWithCode(
                        invitationCode,
                        pendingProfile.getFullName(),
                        pendingProfile.getAvatarUri()
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        companyName -> {
                            isLoading.setValue(false);
                            roleSetupEvent.setValue(new RoleSetupEvent.NavigateToPendingApproval(invitationCode, companyName));
                        },
                        throwable -> handleError(throwable.getMessage())
                ));
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
