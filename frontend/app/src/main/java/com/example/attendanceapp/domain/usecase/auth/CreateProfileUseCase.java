package com.example.attendanceapp.domain.usecase.auth;

import android.content.Context;

import com.example.attendanceapp.core.utils.CloudImageUploader;
import com.example.attendanceapp.data.dto.request.manager.CreateManagerProfileRequest;
import com.example.attendanceapp.data.dto.request.staff.CreateStaffProfileRequest;
import com.example.attendanceapp.data.local.StaffAvatarCache;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class CreateProfileUseCase {

    private final Context context;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    @Inject
    public CreateProfileUseCase(
            @ApplicationContext Context context,
            StaffRepository staffRepository,
            ManagerRepository managerRepository
    ) {
        this.context = context;
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
    }

    /** Tạo hồ sơ Nhân viên. */
    public Completable createStaffProfile(String name, String dob, String gender, String avatar) {
        return Single.fromCallable(() -> {
                    String cleanAvatar = "";
                    if (context != null && avatar != null && !avatar.trim().isEmpty()) {
                        String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatar);
                        cleanAvatar = (cloudUrl != null) ? cloudUrl : "";
                    }
                    return new CreateStaffProfileRequest(
                            requireName(name),
                            toApiDate(dob),
                            toApiGender(gender),
                            cleanAvatar);
                })
                .flatMap(staffRepository::createProfile)
                .ignoreElement();
    }

    /** Cập nhật hồ sơ Nhân viên (fallback khi gặp 409 Conflict). */
    public Completable updateStaffProfile(String name, String dob, String gender, String avatar) {
        return Single.fromCallable(() -> {
                    String cleanAvatar = "";
                    if (context != null && avatar != null && !avatar.trim().isEmpty()) {
                        String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatar);
                        cleanAvatar = (cloudUrl != null) ? cloudUrl : "";
                    }
                    return new com.example.attendanceapp.data.dto.request.staff.UpdateStaffProfileRequest(
                            requireName(name),
                            toApiGender(gender),
                            cleanAvatar,
                            toApiDate(dob)
                    );
                })
                .flatMap(staffRepository::updateProfile)
                .ignoreElement();
    }

    /** Tạo hồ sơ Quản lý (kèm tên công ty). */
    public Completable createManagerProfile(String name, String dob, String gender, String avatar, String companyName) {
        return Single.fromCallable(() -> {
                    if (companyName == null || companyName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Tên công ty không được để trống");
                    }
                    String cleanAvatar = "";
                    if (context != null && avatar != null && !avatar.trim().isEmpty()) {
                        String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatar);
                        cleanAvatar = (cloudUrl != null) ? cloudUrl : "";
                    }

                    // Đúng thứ tự Constructor CreateManagerProfileRequest
                    // (fullName, company, date, gender, avatar)
                    return new CreateManagerProfileRequest(
                            requireName(name),
                            companyName.trim(),
                            toApiDate(dob),
                            toApiGender(gender),
                            cleanAvatar);
                })
                .flatMap(managerRepository::createProfile)
                .ignoreElement();
    }

    /** Mã mời của Quản lý. */
    public Single<String> getInvitationCode() {
        return managerRepository.getInvitationCode();
    }

    /** Nhân viên nhập mã mời để xin tham gia. */
    public Single<String> joinCompanyWithCode(String invitationCode, String name, String avatar) {
        if (invitationCode == null || invitationCode.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Mã mời không được để trống"));
        }
        String cleanAvatar = "";
        if (context != null && avatar != null && !avatar.trim().isEmpty()) {
            String cloudUrl = CloudImageUploader.uploadAvatarSync(context, avatar);
            cleanAvatar = (cloudUrl != null) ? cloudUrl : "";
        }
        return staffRepository.joinWithInvitationCode(invitationCode.trim(), name, cleanAvatar)
                .map(managerInfo -> managerInfo.getCompanyName());
    }

    // --- Helpers ---

    private static String requireName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        return name.trim();
    }

    private static String toApiDate(String dob) {
        if (dob == null || dob.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }
        String cleanDob = dob.trim();

        // 1. Nếu đã là định dạng ISO YYYY-MM-DD
        if (cleanDob.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return cleanDob;
        }

        // 2. Nếu vẫn truyền dạng dd/MM/yyyy thì parse lại sang ISO YYYY-MM-DD
        try {
            return LocalDate.parse(cleanDob, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ");
        }
    }

    private static String toApiGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return "male";
        }
        try {
            return Gender.valueOf(gender.trim().toUpperCase()).name().toLowerCase();
        } catch (Exception e) {
            return gender.trim().toLowerCase();
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
