package com.example.attendanceapp.domain.usecase.profile;

import android.content.Context;
import android.net.Uri;

import com.example.attendanceapp.data.dto.request.manager.UpdateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest;
import com.example.attendanceapp.data.local.StaffAvatarCache;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;

public class UpdateProfileUseCase {

    private final Context context;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    @Inject
    public UpdateProfileUseCase(
            @ApplicationContext Context context,
            StaffRepository staffRepository,
            ManagerRepository managerRepository
    ) {
        this.context = context;
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
    }

    /** companyName chỉ dùng khi role là MANAGER. Repository tự lưu hồ sơ mới vào Room. */
    public Completable execute(String fullName, LocalDate dob, Gender gender, Uri avatarUri,
                               UserRole role, String companyName) {
        return Completable.defer(() -> {
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new IllegalArgumentException("Họ tên không được để trống");
            }
            if (gender == null) {
                throw new IllegalArgumentException("Giới tính không hợp lệ");
            }

            String name = fullName.trim();
            String dobStr = dob != null ? dob.format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
            String genderStr = gender.name().toLowerCase();
            String avatarStr = avatarUri != null ? avatarUri.toString() : "";

            // Chuyển đổi avatar sang Base64 thumbnail để lưu trữ trên Backend (đồng bộ đa thiết bị)
            if (avatarUri != null && context != null) {
                String base64 = StaffAvatarCache.encodeImageToBase64(context, avatarUri.toString());
                if (base64 != null && !base64.isEmpty()) {
                    avatarStr = base64;
                }
            }

            if (role == UserRole.MANAGER) {
                return managerRepository
                        .updateProfile(new UpdateManagerProfileRequest(
                                name, dobStr, genderStr, avatarStr,
                                companyName != null ? companyName.trim() : ""))
                        .ignoreElement();
            }
            return staffRepository
                    .updateProfile(new UpdateStaffProfileRequest(name, dobStr, genderStr, avatarStr))
                    .ignoreElement();
        });
    }
}
