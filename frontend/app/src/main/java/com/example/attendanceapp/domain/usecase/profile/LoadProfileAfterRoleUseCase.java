package com.example.attendanceapp.domain.usecase.profile;

import com.example.attendanceapp.data.remote.ProfileErrors;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

/**
 * Sau khi người dùng chọn Role: gọi đúng API để lấy hồ sơ.
 * - Có hồ sơ: ProfileResult.exists (Role + status đã được Repository lưu vào AppState/Room).
 * - 404: ProfileResult.notFound (ViewModel hỏi "Tạo hồ sơ" hay "Chọn lại vai trò").
 * - Lỗi khác (mạng, 500): đẩy lỗi lên để hiện thông báo + thử lại.
 *
 * Role CHỈ được lưu vào AppState khi hồ sơ tồn tại / vừa tạo xong, nên ViewModel
 * cần giữ role đang chọn trong bộ nhớ cho đến lúc đó.
 */
public class LoadProfileAfterRoleUseCase {

    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    @Inject
    public LoadProfileAfterRoleUseCase(StaffRepository staffRepository, ManagerRepository managerRepository) {
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
    }

    public Single<ProfileResult> execute(UserRole role) {
        Single<UserAccount> call = (role == UserRole.MANAGER)
                ? managerRepository.getProfile()
                : staffRepository.getProfile();

        return call
                .map(ProfileResult::exists)
                .onErrorResumeNext(e -> ProfileErrors.isNotFound(e)
                        ? Single.just(ProfileResult.notFound())
                        : Single.<ProfileResult>error(e));
    }
}