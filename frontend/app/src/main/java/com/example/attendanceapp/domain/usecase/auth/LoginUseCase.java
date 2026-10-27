package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserProfile;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class LoginUseCase {

    private final AuthRepository authRepository;
    private final AppStateRepository appStateRepository;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    @Inject
    public LoginUseCase(AuthRepository authRepository, AppStateRepository appStateRepository,
                        StaffRepository staffRepository, ManagerRepository managerRepository) {
        this.authRepository = authRepository;
        this.appStateRepository = appStateRepository;
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
                    UserRole savedRole = appStateRepository.getUserRole();
                    if (savedRole == UserRole.MANAGER) {
                        return managerRepository.getProfile()
                                .onErrorResumeNext(throwable -> fallbackAccount(phone, UserRole.MANAGER));
                    } else if (savedRole == UserRole.EMPLOYEE) {
                        return staffRepository.getProfile()
                                .onErrorResumeNext(throwable -> fallbackAccount(phone, UserRole.EMPLOYEE));
                    }

                    // Nếu chưa có vai trò được lưu (sau khi đăng xuất hoặc cài đặt lại),
                    // tự động thăm dò profile Quản lý trước, nếu không có thì thăm dò profile Nhân viên
                    return managerRepository.getProfile()
                            .onErrorResumeNext(managerErr -> staffRepository.getProfile())
                            .onErrorResumeNext(bothErr -> {
                                // Cả hai đều chưa tạo hồ sơ -> hiển thị dialog chọn vai trò
                                UserProfile profile = new UserProfile.Builder()
                                        .status(UserStatus.NEEDS_ROLE_SELECTION)
                                        .build();
                                return Single.just(new UserAccount.Builder()
                                        .phoneNumber(phone.trim())
                                        .firstLogin(true)
                                        .profile(profile)
                                        .build());
                            });
                }));
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

    public Single<Boolean> checkPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Vui lòng nhập số điện thoại"));
        }
        return authRepository.checkPhoneExists(phone);
    }
}
