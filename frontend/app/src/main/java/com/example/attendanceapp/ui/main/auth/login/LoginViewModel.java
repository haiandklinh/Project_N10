package com.example.attendanceapp.ui.main.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.domain.model.PendingProfileSetup;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
import com.example.attendanceapp.domain.usecase.auth.LoginUseCase;
import com.example.attendanceapp.domain.usecase.auth.RoleSelectionUseCase;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final RoleSelectionUseCase roleSelectionUseCase;

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
        loginUseCase.execute(phone, password, new AuthCallback<UserAccount>() {
            @Override
            public void onSuccess(UserAccount result) {
                loginUseCase.setLoggedIn(true);
                loginUseCase.setAccountSaved(true);
                isLoading.setValue(false);
                loginState.setValue(new LoginState.Success(result));
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                loginState.setValue(new LoginState.Error(error));
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ LUỒNG TỪ DIALOG & BOTTOM SHEET ---

    public void onRoleSelected(UserRole role) {
        pendingProfile.setRole(role);
        roleSetupEvent.setValue(new RoleSetupEvent.ShowPersonalInfoForm());
    }

    public void onPersonalInfoEntered(String name, String dob, String gender) {
        pendingProfile.updatePersonalInfo(name, dob, gender);

        if (pendingProfile.getRole() == UserRole.MANAGER) {
            roleSetupEvent.setValue(new RoleSetupEvent.ShowCompanyNameForm());
        } else if (pendingProfile.getRole() == UserRole.EMPLOYEE) {
            roleSetupEvent.setValue(new RoleSetupEvent.ShowInvitationCodeForm());
        }
    }

    public void submitManagerCompany(String companyName) {
        isLoading.setValue(true);

        // Bước 1: Cập nhật thông tin cá nhân
        roleSelectionUseCase.updatePersonalInfo(
                pendingProfile.getFullName(),
                pendingProfile.getDob(),
                pendingProfile.getGender(),
                new AuthCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Bước 2: Chọn Role Manager
                        roleSelectionUseCase.selectRole(UserRole.MANAGER, new AuthCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Bước 3: Tạo Công ty
                                roleSelectionUseCase.createCompany(companyName, new AuthCallback<String>() {
                                    @Override
                                    public void onSuccess(String result) {
                                        isLoading.setValue(false);
                                        roleSetupEvent.setValue(new RoleSetupEvent.NavigateToHome(UserRole.MANAGER));
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        handleError(errorMessage);
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                handleError(errorMessage);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        handleError(errorMessage);
                    }
                });
    }

    public void submitEmployeeInvitationCode(String invitationCode) {
        isLoading.setValue(true);

        // Bước 1: Cập nhật thông tin cá nhân
        roleSelectionUseCase.updatePersonalInfo(
                pendingProfile.getFullName(),
                pendingProfile.getDob(),
                pendingProfile.getGender(),
                new AuthCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Bước 2: Chọn Role Employee
                        roleSelectionUseCase.selectRole(UserRole.EMPLOYEE, new AuthCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Bước 3: Tham gia công ty qua mã mời
                                roleSelectionUseCase.joinCompanyWithCode(invitationCode, new AuthCallback<String>() {
                                    @Override
                                    public void onSuccess(String companyName) {
                                        isLoading.setValue(false);
                                        roleSetupEvent.setValue(new RoleSetupEvent.NavigateToPendingApproval(invitationCode, companyName));
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        handleError(errorMessage);
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                handleError(errorMessage);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        handleError(errorMessage);
                    }
                });
    }

    private void handleError(String errorMessage) {
        isLoading.setValue(false);
        roleSetupEvent.setValue(new RoleSetupEvent.ShowError(errorMessage));
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