package com.example.attendanceapp.domain.usecase.auth;

import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.UserRepository;

import java.time.LocalDate;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class RoleSelectionUseCase {
    private final UserRepository userRepository;

    @Inject
    public RoleSelectionUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Chọn vai trò cho người dùng (gọi qua UserRepository theo Clean Architecture)
     */
    public Completable selectRole(UserRole role) {
        return userRepository.selectRole(role);
    }

    /**
     * Cập nhật thông tin cá nhân (Tạo hồ sơ)
     */
    public Completable updatePersonalInfo(String name, String dob, String gender, String avatarUri, UserRole role, String companyName) {
        java.time.LocalDate date = null;
        try {
            date = java.time.LocalDate.parse(dob, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {}

        com.example.attendanceapp.domain.model.Gender g = null;
        try {
            g = com.example.attendanceapp.domain.model.Gender.valueOf(gender.toUpperCase());
        } catch (Exception ignored) {}

        android.net.Uri uri = (avatarUri != null && !avatarUri.isEmpty()) ? android.net.Uri.parse(avatarUri) : null;
        return userRepository.createProfile(name, date, g, uri, role, companyName);
    }

    /**
     * Tạo công ty mới (cho Manager)
     */
    public Single<String> createCompany(String companyName) {
        return userRepository.updateCompanyName(companyName)
                .andThen(userRepository.getInvitationCode());
    }

    /**
     * Tham gia công ty bằng mã mời (cho Employee)
     */
    public Single<String> joinCompanyWithCode(String invitationCode, String name, String avatar) {
        return userRepository.joinManagerWithCode(invitationCode, name, avatar)
                .map(managerInfo -> managerInfo.getCompanyName());
    }
}
