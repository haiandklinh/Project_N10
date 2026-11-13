package com.example.attendanceapp.domain.usecase.profile;

import android.content.Context;
import android.net.Uri;

import com.example.attendanceapp.core.utils.CloudImageUploader;
import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest;
import com.example.attendanceapp.data.local.StaffAvatarCache;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UpdateProfileUseCase {

    private final Context context;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;
    private final SessionRepository sessionRepository;

    @Inject
    public UpdateProfileUseCase(
            @ApplicationContext Context context,
            StaffRepository staffRepository,
            ManagerRepository managerRepository,
            SessionRepository sessionRepository
    ) {
        this.context = context;
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
        this.sessionRepository = sessionRepository;
    }

    /** companyName chỉ dùng khi role là MANAGER. Repository tự lưu hồ sơ mới vào Room. */
    public Completable execute(String fullName, LocalDate dob, Gender gender, Uri avatarUri,
                               UserRole role, String companyName) {
        return sessionRepository.getCurrentUser()
                .map(java.util.Optional::of)
                .defaultIfEmpty(java.util.Optional.empty())
                .flatMapCompletable(optUser -> {
                    if (fullName == null || fullName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Họ tên không được để trống");
                    }
                    if (gender == null) {
                        throw new IllegalArgumentException("Giới tính không hợp lệ");
                    }

                    String name = fullName.trim();
                    String dobStr = dob != null ? dob.format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
                    String genderStr = gender.name().toLowerCase();

                    // Xác định avatar:
                    String avatarStr = null;
                    if (avatarUri != null && !avatarUri.toString().trim().isEmpty()) {
                        // Người dùng có chọn ảnh mới -> upload lên Cloud lấy URL ngắn
                        String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatarUri.toString());
                        avatarStr = (cloudUrl != null) ? cloudUrl : "";
                    } else if (optUser.isPresent() && optUser.get().getProfile() != null) {
                        // Người dùng không đổi avatar -> giữ nguyên avatar đã có, không được ghi đè rỗng
                        avatarStr = optUser.get().getProfile().getAvatarUrl();
                    }
                    if (avatarStr == null) {
                        avatarStr = "";
                    }

                    if (role == UserRole.MANAGER) {
                        return managerRepository
                                .updateProfile(new UpdateManagerProfileRequest(
                                        name, genderStr, companyName != null ? companyName.trim() : "",
                                        avatarStr, dobStr))
                                .ignoreElement();
                    }

                    return staffRepository
                            .updateProfile(new UpdateStaffProfileRequest(name, genderStr, avatarStr, dobStr))
                            .ignoreElement();
                });
    }

    /**
     * Cập nhật nhanh chỉ ảnh đại diện cho tài khoản hiện tại, đồng bộ lên Cloud và Server
     */
    public Completable updateAvatar(Uri avatarUri, UserRole role) {
        return Completable.defer(() -> {
            if (avatarUri == null || context == null) {
                return Completable.complete();
            }

            // Upload ảnh lên Cloud lấy URL ngắn (dưới 50 ký tự, phù hợp cột VARCHAR(255) trong CSDL)
            String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatarUri.toString());
            String finalAvatar = (cloudUrl != null) ? cloudUrl : "";

            return sessionRepository.getCurrentUser()
                    .switchIfEmpty(Single.error(new IllegalStateException("Không tìm thấy thông tin tài khoản")))
                    .flatMapCompletable(user -> {
                        String name = user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName().trim() : "Người dùng";
                        String dobStr = user.getDateOfBirth() != null ? user.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
                        String genderStr = user.getGender() != null ? user.getGender().name().toLowerCase() : "male";
                        StaffAvatarCache.saveAvatar(context, name, user.getPhoneNumber(), finalAvatar);

                        if (role == UserRole.MANAGER) {
                            String company = user.getCompanyName() != null ? user.getCompanyName() : "";
                            return managerRepository.updateProfile(new UpdateManagerProfileRequest(
                                    name, genderStr, company, finalAvatar, dobStr
                            )).ignoreElement();
                        } else {
                            return staffRepository.updateProfile(new UpdateStaffProfileRequest(
                                    name, genderStr, finalAvatar, dobStr
                            )).ignoreElement();
                        }
                    });
        });
    }
}
