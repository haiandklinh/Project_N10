package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserProfile;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginUseCase {

    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;
    private final SessionRepository sessionRepository;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    @Inject
    public LoginUseCase(AuthRepository authRepository, AppStateRepository appStateRepository,
                        SessionRepository sessionRepository, StaffRepository staffRepository,
                        ManagerRepository managerRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
        this.sessionRepository = sessionRepository;
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
    }

    /**
     * Thực hiện Đăng nhập bằng Số điện thoại và Mật khẩu theo chuẩn RxJava.
     */
    public Single<UserAccount> execute(String phone, String password) {
        if (phone == null || phone.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập số điện thoại"));
        }
        if (password == null || password.isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập mật khẩu"));
        }

        return authRepository.login(phone, password)
                .doOnComplete(() -> {
                    appStateRepository.setLoggedIn(true);
                    appStateRepository.setAccountSaved(true);
                })
                .andThen(Single.defer(() -> {
                    UserRole appRole = appStateRepository.getUserRole();
                    RoleHolder fallbackRole = new RoleHolder(appRole);
                    return sessionRepository.getCurrentUser()
                            .map(user -> new RoleHolder(user.getRole()))
                            .filter(holder -> holder.role != null)
                            .defaultIfEmpty(fallbackRole)
                            .onErrorReturnItem(fallbackRole)
                            .flatMap(holder -> loadAccountForRole(phone, holder.role));
                }));
    }

    private Single<UserAccount> loadAccountForRole(String phone, UserRole savedRole) {
        if (savedRole == UserRole.MANAGER) {
            return managerRepository.getProfile()
                    .onErrorResumeNext(throwable -> fallbackAccount(phone, UserRole.MANAGER));
        }
        if (savedRole == UserRole.EMPLOYEE) {
            return staffRepository.getProfile()
                    .flatMap(userAccount -> Single.fromCallable(() -> {
                                String code = sessionRepository.getCachedInvitationCode().blockingGet();
                                return code != null ? code.trim() : "";
                            })
                            .subscribeOn(Schedulers.io())
                            .flatMap(inviteCode -> {
                                if (inviteCode.isEmpty()) {
                                    return Single.just(userAccount);
                                }
                                return staffRepository.checkCompanyApprovalStatus(inviteCode)
                                        .map(isApproved -> {
                                            if (!isApproved) {
                                                UserProfile profile = new UserProfile.Builder()
                                                        .userId(userAccount.getProfile().getUserId())
                                                        .fullName(userAccount.getProfile().getFullName())
                                                        .gender(userAccount.getProfile().getGender())
                                                        .dateOfBirth(userAccount.getProfile().getDateOfBirth())
                                                        .avatarUrl(userAccount.getProfile().getAvatarUrl())
                                                        .role(UserRole.EMPLOYEE)
                                                        .status(UserStatus.PENDING_APPROVAL)
                                                        .companyName(userAccount.getProfile().getCompanyName())
                                                        .build();
                                                return userAccount.withProfile(profile);
                                            }
                                            return userAccount;
                                        });
                            }))
                    .onErrorResumeNext(throwable -> fallbackAccount(phone, UserRole.EMPLOYEE));
        }

        // Tài khoản mới chưa có vai trò đã lưu: dò hồ sơ trước khi yêu cầu chọn vai trò.
        return managerRepository.getProfile()
                .onErrorResumeNext(managerErr -> staffRepository.getProfile()
                        .flatMap(userAccount -> Single.fromCallable(() -> {
                                    String code = sessionRepository.getCachedInvitationCode().blockingGet();
                                    return code != null ? code.trim() : "";
                                })
                                .subscribeOn(Schedulers.io())
                                .flatMap(inviteCode -> {
                                    if (inviteCode.isEmpty()) {
                                        return Single.just(userAccount);
                                    }
                                    return staffRepository.checkCompanyApprovalStatus(inviteCode)
                                            .map(isApproved -> {
                                                if (!isApproved) {
                                                    UserProfile profile = new UserProfile.Builder()
                                                            .userId(userAccount.getProfile().getUserId())
                                                            .fullName(userAccount.getProfile().getFullName())
                                                            .gender(userAccount.getProfile().getGender())
                                                            .dateOfBirth(userAccount.getProfile().getDateOfBirth())
                                                            .avatarUrl(userAccount.getProfile().getAvatarUrl())
                                                            .role(UserRole.EMPLOYEE)
                                                            .status(UserStatus.PENDING_APPROVAL)
                                                            .companyName(userAccount.getProfile().getCompanyName())
                                                            .build();
                                                    return userAccount.withProfile(profile);
                                                }
                                                return userAccount;
                                            });
                                })))
                .onErrorResumeNext(bothErr -> {
                    UserRole savedRoleFallback = appStateRepository.getUserRole();
                    if (savedRoleFallback != null) {
                        return fallbackAccount(phone, savedRoleFallback);
                    }
                    return sessionRepository.getCurrentUser()
                            .map(user -> user.getRole() != null ? user.getRole() : UserRole.EMPLOYEE)
                            .defaultIfEmpty(UserRole.EMPLOYEE)
                            .flatMap(role -> fallbackAccount(phone, role));
                });
    }

    private Single<UserAccount> fallbackAccount(String phone, UserRole role) {
        UserProfile profile = new UserProfile.Builder()
                .role(role)
                .status(UserStatus.APPROVED)
                .build();
        return Single.just(new UserAccount.Builder()
                .phoneNumber(phone.trim())
                .firstLogin(false)
                .profile(profile)
                .build());
    }

    private static final class RoleHolder {
        private final UserRole role;

        private RoleHolder(UserRole role) {
            this.role = role;
        }
    }

    public Single<Boolean> checkPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập số điện thoại"));
        }
        return authRepository.checkPhoneExists(phone);
    }
}
