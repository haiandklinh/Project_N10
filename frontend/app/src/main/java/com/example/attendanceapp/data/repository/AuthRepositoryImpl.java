package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.auth.CheckPhoneRequest;
import com.example.attendanceapp.data.dto.request.auth.LoginRequest;
import com.example.attendanceapp.data.dto.request.auth.RefreshTokenRequest;
import com.example.attendanceapp.data.dto.request.auth.RegisterRequest;
import com.example.attendanceapp.data.dto.request.auth.ResetPasswordRequest;
import com.example.attendanceapp.data.dto.request.auth.SendOtpRequest;
import com.example.attendanceapp.data.dto.request.auth.VerifyOtpRequest;
import com.example.attendanceapp.data.dto.response.auth.CheckPhoneResponse;
import com.example.attendanceapp.data.dto.response.auth.SendOtpResponse;
import com.example.attendanceapp.data.dto.response.auth.VerifyOtpResponse;
import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.data.mapper.UserMapper;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.AuthApi;
import com.example.attendanceapp.data.remote.UserApi;
import com.example.attendanceapp.domain.exception.AppException;
import com.example.attendanceapp.domain.model.OtpPurpose;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    // Chuỗi role truyền cho UserMapper. Đổi "EMPLOYEE" thành "STAFF" nếu enum UserRole của bạn đặt tên như vậy.
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_STAFF = "EMPLOYEE";

    private final AuthApi authApi;
    private final UserApi userApi;
    private final TokenManager tokenManager;
    private final UserRepository userRepository;
    private final AppStateRepository appStateRepository;

    @Inject
    public AuthRepositoryImpl(AuthApi authApi, UserApi userApi,
                              TokenManager tokenManager,
                              UserRepository userRepository,
                              AppStateRepository appStateRepository) {
        this.authApi = authApi;
        this.userApi = userApi;
        this.tokenManager = tokenManager;
        this.userRepository = userRepository;
        this.appStateRepository = appStateRepository;
    }

    @Override
    public Single<Boolean> checkPhoneExists(String phoneNumber) {
        return authApi.checkPhone(new CheckPhoneRequest(phoneNumber))
                .map(CheckPhoneResponse::isExist)
                .compose(ApiErrorMapper.forSingle("Kiểm tra số điện thoại thất bại"));
    }

    @Override
    public Completable register(String phoneNumber, String password) {
        return authApi.register(new RegisterRequest(phoneNumber, password))
                .compose(ApiErrorMapper.forCompletable("Đăng ký thất bại"));
    }

    @Override
    public Single<UserAccount> login(String phoneNumber, String password) {
        return authApi.login(new LoginRequest(phoneNumber, password))
                .flatMap(auth -> {
                    // 1. Lưu token
                    tokenManager.saveTokens(auth.getAccessToken(), auth.getRefreshToken());

                    // 2. Backend chưa có GET /me nên xác định hồ sơ + vai trò
                    //    bằng cách gọi lần lượt profile Manager rồi Staff
                    return loadAccount(phoneNumber);
                })
                // 3. Lưu session vào local storage (Room DB)
                .flatMap(account -> userRepository.saveUserSession(account)
                        .toSingleDefault(account))

                // 4. Lưu trạng thái app (isLoggedIn, userStatus, userRole)
                .doOnSuccess(account -> {
                    appStateRepository.setLoggedIn(true);
                    appStateRepository.saveUserStatus(account.getProfile().getStatus());
                    if (account.getProfile().getRole() != null) {
                        appStateRepository.saveUserRole(account.getProfile().getRole());
                    }
                })

                // 5. Nếu xảy ra bất kỳ lỗi nào, xóa token để tránh trạng thái dở dang
                .doOnError(e -> tokenManager.clearTokens())

                // 6. Chuyển đổi lỗi thân thiện cho UI
                .compose(ApiErrorMapper.forSingle("Số điện thoại hoặc mật khẩu không chính xác"));
    }

    /**
     * Thử profile Manager -> nếu không có thì thử profile Staff
     * -> nếu cả hai đều không có thì coi là đăng nhập lần đầu (chưa có hồ sơ).
     * Các lỗi khác (401, 500, mất mạng...) được ném ra bình thường.
     */
    private Single<UserAccount> loadAccount(String phoneNumber) {
        return userApi.getManagerProfile()
                .map(p -> UserMapper.toDomain(phoneNumber, ROLE_MANAGER, UserMapper.toUserProfile(p)))
                .onErrorResumeNext(e -> isProfileMissing(e)
                        ? userApi.getStaffProfile()
                                .map(p -> UserMapper.toDomain(phoneNumber, ROLE_STAFF, UserMapper.toUserProfile(p)))
                        : Single.<UserAccount>error(e))
                .onErrorResumeNext(e -> isProfileMissing(e)
                        ? Single.just(buildFirstLoginAccount(phoneNumber))
                        : Single.<UserAccount>error(e));
    }

    private UserAccount buildFirstLoginAccount(String phoneNumber) {
        return new UserAccount.Builder()
                .phoneNumber(phoneNumber)
                .firstLogin(true)
                .profile(UserMapper.toUserProfile())
                .build();
    }

    /** 404: chưa có hồ sơ loại này. 403: tài khoản thuộc loại khác (nếu BE trả vậy). */
    private boolean isProfileMissing(Throwable e) {
        if (!(e instanceof HttpException)) return false;
        int code = ((HttpException) e).code();
        return code == 404 || code == 403;
    }

    @Override
    public Completable logout() {
        return Completable.defer(() -> {
            String refreshToken = tokenManager.getRefreshToken();
            Completable remote = refreshToken == null
                    ? Completable.complete()
                    // Server lỗi hay mất mạng vẫn tiếp tục xóa dữ liệu trên máy
                    : authApi.logout(new RefreshTokenRequest(refreshToken)).onErrorComplete();

            return remote
                    .andThen(Completable.fromAction(() -> {
                        tokenManager.clearTokens();
                        appStateRepository.setLoggedIn(false);
                        appStateRepository.clearSession();
                        // KHÔNG xóa Room DB để giữ profile cho Login Saved Account
                    }));
        });
    }

    @Override
    public Single<String> sendOtp(String phoneNumber, OtpPurpose purpose) {
        if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
            return Single.error(new IllegalArgumentException("Số điện thoại không hợp lệ"));
        }
        SendOtpRequest request = new SendOtpRequest(phoneNumber);
        Single<SendOtpResponse> call = purpose == OtpPurpose.REGISTER
                ? authApi.sendRegisterOtp(request)
                : authApi.sendForgotPasswordOtp(request);
        return call.map(res -> res.getOtp() != null ? res.getOtp() : "")
                .compose(ApiErrorMapper.forSingle("Gửi OTP thất bại"));
    }

    @Override
    public Single<String> verifyOtp(String phoneNumber, String otp, OtpPurpose purpose) {
        VerifyOtpRequest request = new VerifyOtpRequest(phoneNumber, otp);
        Single<VerifyOtpResponse> call = purpose == OtpPurpose.REGISTER
                ? authApi.verifyRegisterOtp(request)
                : authApi.verifyForgotPasswordOtp(request);

        return call
                .flatMap(res -> res.isVerified()
                        ? Single.just(res.getResetToken() != null ? res.getResetToken() : phoneNumber)
                        : Single.<String>error(
                        new AppException("Mã OTP không đúng hoặc đã hết hạn", 400, "INVALID_OTP")))
                .compose(ApiErrorMapper.forSingle("Mã OTP không đúng hoặc đã hết hạn"));
    }

    @Override
    public Completable resetPassword(String phoneNumber, String newPassword) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Số điện thoại không hợp lệ"));
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Completable.error(new IllegalArgumentException("Mật khẩu phải chứa ít nhất 6 ký tự"));
        }
        return authApi.resetPassword(new ResetPasswordRequest(phoneNumber, newPassword))
                .compose(ApiErrorMapper.forCompletable("Đặt lại mật khẩu thất bại"));
    }
}