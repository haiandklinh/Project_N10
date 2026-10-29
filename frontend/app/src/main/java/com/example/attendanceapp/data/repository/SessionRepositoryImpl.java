package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.response.manager.ManagerProfileResponse;
import com.example.attendanceapp.data.dto.response.staff.StaffProfileResponse;
import com.example.attendanceapp.data.local.HandledRequestManager;
import com.example.attendanceapp.data.local.dao.NotificationDao;
import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.data.mapper.UserMapper;
import com.example.attendanceapp.domain.model.SavedAccountData;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserProfile;
import com.example.attendanceapp.domain.repository.SessionRepository;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class SessionRepositoryImpl implements SessionRepository {

    private final UserDao userDao;
    private final HandledRequestManager handledRequestManager;
    private final NotificationDao notificationDao;

    @Inject
    public SessionRepositoryImpl(
            UserDao userDao,
            HandledRequestManager handledRequestManager,
            NotificationDao notificationDao
    ) {
        this.userDao = userDao;
        this.handledRequestManager = handledRequestManager;
        this.notificationDao = notificationDao;
    }

    @Override
    public Completable savePhoneNumber(String phoneNumber) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .flatMapCompletable(opt -> {
                    UserEntity entity = opt.orElseGet(() -> new UserEntity("", "", phoneNumber, "", "NEEDS_ROLE_SELECTION"));
                    entity.setPhone(phoneNumber);
                    return Completable.fromAction(() -> userDao.insertOrUpdateUser(entity));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<UserAccount> saveStaffProfile(StaffProfileResponse profile) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .map(opt -> {
                    UserEntity old = opt.orElse(null);
                    String phone = (old != null && old.getPhone() != null) ? old.getPhone() : "";
                    String inviteCode = (old != null ? old.getInvitationCode() : null);
                    String companyName = (old != null ? old.getCompanyName() : null);
                    String companyId = (old != null ? old.getCompanyId() : null);

                    UserProfile userProfile = UserMapper.toUserProfile(profile);
                    UserAccount account = new UserAccount.Builder()
                            .phoneNumber(phone)
                            .invitationCode(inviteCode)
                            .profile(userProfile)
                            .build();

                    UserEntity entityToSave = UserMapper.toEntity(account);
                    if (companyName != null && !companyName.isEmpty()) entityToSave.setCompanyName(companyName);
                    if (companyId != null && !companyId.isEmpty()) entityToSave.setCompanyId(companyId);
                    if ((entityToSave.getAvatarUrl() == null || entityToSave.getAvatarUrl().isEmpty())
                            && old != null && old.getAvatarUrl() != null && !old.getAvatarUrl().isEmpty()) {
                        entityToSave.setAvatarUrl(old.getAvatarUrl());
                    }
                    if (old != null && old.getId() != 0) {
                        entityToSave.setId(old.getId());
                    }
                    userDao.insertOrUpdateUser(entityToSave);
                    return account;
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<UserAccount> saveManagerProfile(ManagerProfileResponse profile) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .map(opt -> {
                    UserEntity old = opt.orElse(null);
                    String phone = (old != null && old.getPhone() != null) ? old.getPhone() : "";
                    String inviteCode = (old != null ? old.getInvitationCode() : null);

                    UserProfile userProfile = UserMapper.toUserProfile(profile);
                    UserAccount account = new UserAccount.Builder()
                            .phoneNumber(phone)
                            .invitationCode(inviteCode)
                            .profile(userProfile)
                            .build();

                    UserEntity entityToSave = UserMapper.toEntity(account);
                    if ((entityToSave.getAvatarUrl() == null || entityToSave.getAvatarUrl().isEmpty())
                            && old != null && old.getAvatarUrl() != null && !old.getAvatarUrl().isEmpty()) {
                        entityToSave.setAvatarUrl(old.getAvatarUrl());
                    }
                    if (old != null && old.getId() != 0) {
                        entityToSave.setId(old.getId());
                    }
                    userDao.insertOrUpdateUser(entityToSave);
                    return account;
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<UserAccount> getCurrentUser() {
        return userDao.getCurrentUserSingle().map(UserMapper::fromEntity);
    }

    @Override
    public Observable<UserAccount> observeCurrentUser() {
        return userDao.getCurrentUser()
                .map(UserMapper::fromEntity)
                .toObservable();
    }

    @Override
    public Maybe<String> getCachedInvitationCode() {
        return userDao.getCurrentUserSingle()
                .map(UserEntity::getInvitationCode)
                .filter(code -> code != null && !code.isEmpty());
    }

    @Override
    public Completable saveInvitationCode(String code) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .flatMapCompletable(opt -> {
                    UserEntity entity = opt.orElseGet(() -> new UserEntity("", "", "", "", "ACTIVE"));
                    entity.setInvitationCode(code);
                    return Completable.fromAction(() -> userDao.insertOrUpdateUser(entity));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable savePendingJoinRequest(String invitationCode, String companyName) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .flatMapCompletable(opt -> {
                    UserEntity entity = opt.orElseGet(() -> new UserEntity("", "", "", "EMPLOYEE", "PENDING_APPROVAL"));
                    entity.setInvitationCode(invitationCode);
                    entity.setCompanyName(companyName);
                    entity.setStatus("PENDING_APPROVAL");
                    return Completable.fromAction(() -> userDao.insertOrUpdateUser(entity));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable saveApprovalSuccess(String managerId) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .flatMapCompletable(opt -> {
                    UserEntity entity = opt.orElseGet(() -> new UserEntity("", "", "", "EMPLOYEE", "APPROVED"));
                    entity.setCompanyId(managerId);
                    entity.setStatus("APPROVED");
                    return Completable.fromAction(() -> userDao.insertOrUpdateUser(entity));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable saveSelectedCompany(String companyId, String companyName, String position) {
        return userDao.getCurrentUserSingle()
                .map(Optional::of)
                .onErrorReturnItem(Optional.empty())
                .defaultIfEmpty(Optional.empty())
                .flatMapCompletable(opt -> {
                    UserEntity entity = opt.orElseGet(() -> new UserEntity("", "", "", "EMPLOYEE", "APPROVED"));
                    entity.setCompanyId(companyId);
                    entity.setCompanyName(companyName);
                    entity.setStatus("APPROVED");
                    return Completable.fromAction(() -> userDao.insertOrUpdateUser(entity));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<SavedAccountData> getSavedAccountData() {
        return userDao.getCurrentUserSingle()
                .map(entity -> new SavedAccountData(
                        entity.getName() != null && !entity.getName().trim().isEmpty() ? entity.getName() : "Người dùng",
                        entity.getCompanyName() != null && !entity.getCompanyName().trim().isEmpty() ? entity.getCompanyName() : "Công ty",
                        entity.getInvitationCode() != null && !entity.getInvitationCode().trim().isEmpty() ? entity.getInvitationCode() : "CODE",
                        entity.getPhone(),
                        entity.getAvatarUrl()
                ));
    }

    @Override
    public Completable clear() {
        return userDao.getCurrentUserSingle()
                .flatMapCompletable(entity -> {
                    String phone = entity.getPhone();
                    String name = entity.getName();
                    String companyName = entity.getCompanyName();
                    String companyId = entity.getCompanyId();
//                    String invitationCode = entity.getInvitationCode();
                    String invitationCode = null;
                    String avatarUrl = entity.getAvatarUrl();
                    String role = entity.getRole();
                    String dob = entity.getDateOfBirth();
                    String gender = entity.getGender();

                    // Xóa toàn bộ dữ liệu người dùng và thông báo phiên làm việc
                    return Completable.fromAction(() -> {
                                userDao.clearAllUsers();
                                if (handledRequestManager != null) {
                                    handledRequestManager.clear();
                                }
                            })
                            .andThen(notificationDao.clearAll())
                            .andThen(Completable.defer(() -> {
                                // Tạo lại entity với thông tin tài khoản được giữ lại cho màn hình Đăng nhập tài khoản đã lưu
                                UserEntity newEntity = new UserEntity(
                                        entity.getUserId() != null ? entity.getUserId() : "",
                                        name != null ? name : "",
                                        phone != null ? phone : "",
                                        role != null ? role : "",
                                        "NEEDS_ROLE_SELECTION",
                                        dob,
                                        gender,
                                        avatarUrl,
                                        companyId,
                                        companyName,
                                        invitationCode,
                                        false,
                                        true
                                );
                                return Completable.fromAction(() -> userDao.insertOrUpdateUser(newEntity));
                            }));
                })
                .onErrorResumeNext(throwable -> {
                    // Nếu không có user cũ, chỉ clear bình thường
                    return Completable.fromAction(() -> {
                        userDao.clearAllUsers();
                        if (handledRequestManager != null) {
                            handledRequestManager.clear();
                        }
                    }).andThen(notificationDao.clearAll());
                })
                .subscribeOn(Schedulers.io());
    }
}
