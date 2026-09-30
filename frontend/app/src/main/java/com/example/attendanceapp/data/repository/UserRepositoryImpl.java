package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.profile.ProfileRequest;
import com.example.attendanceapp.data.dto.request.profile.JoinCompanyRequest;
import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.data.mapper.UserMapper;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.UserApi;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.ManagerInfo;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

@Singleton
public class UserRepositoryImpl implements UserRepository {

    private final UserApi userApi;
    private final UserDao userDao;
    private final BehaviorSubject<Boolean> loggedInSubject = BehaviorSubject.createDefault(false);
    private final com.example.attendanceapp.domain.repository.AppStateRepository appStateRepository;

    private final android.content.Context context;

    @Inject
    public UserRepositoryImpl(android.content.Context context, UserApi userApi, UserDao userDao, com.example.attendanceapp.domain.repository.AppStateRepository appStateRepository) {
        this.context = context;
        this.userApi = userApi;
        this.userDao = userDao;
        this.appStateRepository = appStateRepository;
    }

    @Override
    public Maybe<UserRole> getSavedUserRole() {
        return userDao.getCurrentUserSingle()
                .map(UserMapper::fromEntity)
                .map(UserAccount::getRole)
                .filter(role -> role != null);
    }

    @Override
    public Completable saveUserRole(UserRole role) {
        return userDao.getCurrentUserSingle()
                .flatMapCompletable(entity -> {
                    entity.setRole(role != null ? role.name() : null);
                    return Completable.fromAction(() -> userDao.updateUser(entity));
                });
    }

    @Override
    public Maybe<UserAccount> getCurrentUser() {
        return userDao.getCurrentUserSingle()
                .map(UserMapper::fromEntity);
    }

    @Override
    public Observable<UserAccount> observeCurrentUser() {
        return userDao.getCurrentUser()
                .map(UserMapper::fromEntity)
                .toObservable();
    }

    @Override
    public Completable saveUserSession(UserAccount userAccount) {
        return Completable.fromAction(() -> {
            UserEntity entity = UserMapper.toEntity(userAccount);
            userDao.insertOrUpdateUser(entity);
            loggedInSubject.onNext(true);
        });
    }

    @Override
    public Single<Boolean> isLoggedIn() {
        return userDao.getCurrentUserSingle()
                .map(entity -> true)
                .toSingle();
    }

    @Override
    public Observable<Boolean> observeLoggedIn() {
        return loggedInSubject;
    }

    @Override
    public Completable clearUserSession() {
        return Completable.fromAction(() -> {
            userDao.clearAllUsers();
            appStateRepository.setLoggedIn(false);
            loggedInSubject.onNext(false);
        }).subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io());
    }

    @Override
    public Completable selectRole(UserRole role) {
        return saveUserRole(role);
    }

    @Override
    public Completable updatePersonalInfo(String fullName, LocalDate dob, Gender gender, android.net.Uri avatarUri, UserRole role) {
        String dobStr = dob != null ? dob.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        String genderStr = gender != null ? gender.name().toLowerCase() : "male";
        String roleStr = (role != null) ? role.name() : "";

        return Single.fromCallable(() -> {
                    if (avatarUri != null) {
                        java.io.File file = com.example.attendanceapp.core.utils.FileUtils.getFileFromUri(context, avatarUri);
                        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(file, okhttp3.MediaType.parse(context.getContentResolver().getType(avatarUri)));
                        return okhttp3.MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
                    }
                    return null;
                })
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .flatMap(avatarPart -> {
                    okhttp3.RequestBody namePart = okhttp3.RequestBody.create(fullName, okhttp3.MediaType.parse("text/plain"));
                    okhttp3.RequestBody datePart = okhttp3.RequestBody.create(dobStr, okhttp3.MediaType.parse("text/plain"));
                    okhttp3.RequestBody genderPart = okhttp3.RequestBody.create(genderStr, okhttp3.MediaType.parse("text/plain"));

                    if (avatarPart != null) {
                        return userApi.createProfile(namePart, datePart, genderPart, avatarPart);
                    } else {
                        ProfileRequest request = new ProfileRequest(fullName, dobStr, genderStr, "");
                        return userApi.createProfile(request);
                    }
                })
                .compose(ApiErrorMapper.forSingle("Cập nhật thông tin cá nhân thất bại"))
                .flatMapCompletable(profileResponse ->
                        userDao.getCurrentUserSingle()
                                .flatMapCompletable(entity -> {
                                    entity.setName(fullName);
                                    entity.setDateOfBirth(dobStr);
                                    entity.setGender(genderStr);
                                    entity.setRole(roleStr);
                                    entity.setAvatarUrl(profileResponse.getAvatarUrl());
                                    entity.setStatus(profileResponse.getStatus());
                                    return Completable.fromAction(() -> userDao.updateUser(entity));
                                })
                );
    }

    @Override
    public Completable updateCompanyName(String companyName) {
        return userDao.getCurrentUserSingle()
                .flatMapCompletable(entity -> {
                    entity.setCompanyName(companyName);
                    return Completable.fromAction(() -> userDao.updateUser(entity));
                });
    }

    @Override
    public Single<String> getInvitationCode() {
        return userDao.getCurrentUserSingle()
                .map(UserEntity::getInvitationCode)
                .filter(code -> code != null && !code.isEmpty())
                .switchIfEmpty(Single.defer(() -> 
                    userApi.getInvitationCode()
                            .map(messageResponse -> messageResponse != null ? messageResponse.getMessage() : "")
                            .compose(ApiErrorMapper.forSingle("Lấy mã mời thất bại"))
                ));
    }

    @Override
    public Single<String> regenerateInvitationCode() {
        return userApi.regenerateInvitationCode()
                .map(messageResponse -> messageResponse != null ? messageResponse.getMessage() : "")
                .compose(ApiErrorMapper.forSingle("Tạo mã mời mới thất bại"))
                .flatMap(newCode -> 
                    userDao.getCurrentUserSingle()
                        .flatMapCompletable(entity -> {
                            entity.setInvitationCode(newCode);
                            return Completable.fromAction(() -> userDao.updateUser(entity));
                        })
                        .toSingle(() -> newCode)
                );
    }

    @Override
    public Single<ManagerInfo> joinManagerWithCode(String otp, String name, String avatar) {
        return userApi.joinManagerWithCode(new JoinCompanyRequest(otp, name, avatar))
                .map(response -> new ManagerInfo(
                        response.getManagerId(),
                        response.getManagerName(),
                        response.getCompanyName(),
                        response.getAvatarUrl()
                ))
                .compose(ApiErrorMapper.forSingle("Tham gia công ty thất bại"));
    }
}
