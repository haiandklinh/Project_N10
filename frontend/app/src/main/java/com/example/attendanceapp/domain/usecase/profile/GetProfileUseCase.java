package com.example.attendanceapp.domain.usecase.profile;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserProfile;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

/**
 * Lấy hồ sơ mới nhất từ server theo role đang đăng nhập (Repository tự cập nhật Room).
 * Mất mạng hoặc lỗi server thì trả hồ sơ đang lưu trong Room; Room cũng trống thì báo lỗi gốc.
 */
public class GetProfileUseCase {

    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;
    private final AppStateRepository appStateRepository;
    private final SessionRepository sessionRepository;

    @Inject
    public GetProfileUseCase(StaffRepository staffRepository,
                             ManagerRepository managerRepository,
                             AppStateRepository appStateRepository,
                             SessionRepository sessionRepository) {
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
        this.appStateRepository = appStateRepository;
        this.sessionRepository = sessionRepository;
    }

    public Single<UserProfile> execute() {
        return Single.defer(() -> {
            UserRole role = appStateRepository.getUserRole();
            if (role == null) {
                return Single.<UserProfile>error(new IllegalStateException("Chưa chọn vai trò"));
            }

            Single<UserAccount> remote = (role == UserRole.MANAGER)
                    ? managerRepository.getProfile()
                    : staffRepository.getProfile();

            return remote
                    .map(UserAccount::getProfile)
                    .onErrorResumeNext(error -> sessionRepository.getCurrentUser()
                            .map(UserAccount::getProfile)
                            .switchIfEmpty(Single.<UserProfile>error(error)));
        });
    }
}