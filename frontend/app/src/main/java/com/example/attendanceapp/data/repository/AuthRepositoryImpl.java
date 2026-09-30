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
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

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

                    // 2. Trích xuất vai trò từ Access Token
                    String roleStr = extractRoleFromToken(auth.getAccessToken());

                    final String finalRoleStr = roleStr;
                    
                    // 3. Lấy thông tin cá nhân và map sang Domain
                    return userApi.getMe()
                            .map(meResponse -> {
                                UserAccount account = UserMapper.toDomain(meResponse, phoneNumber, finalRoleStr);
                                // Xử lý logic firstLogin: Nếu đã có Role thì không phải login lần đầu
                                if (finalRoleStr != null) {
                                    account = new UserAccount.Builder()
                                            .phoneNumber(account.getPhoneNumber())
                                            .firstLogin(false)
                                            .profile(account.getProfile())
                                            .build();
                                }
                                return account;
                            });
                })
                // 4. Lưu session vào local storage (Room DB)
                .flatMap(account -> userRepository.saveUserSession(account)
                        .toSingleDefault(account))

                // 5. Lưu trạng thái app (isLoggedIn, userStatus, userRole) vào AppStateRepository
                .doOnSuccess(account -> {
                    appStateRepository.setLoggedIn(true);
                    appStateRepository.saveUserStatus(account.getProfile().getStatus());
                    if (account.getProfile().getRole() != null) {
                        appStateRepository.saveUserRole(account.getProfile().getRole());
                    }
                })

                // 6. Nếu xảy ra bất kỳ lỗi nào, xóa token để tránh trạng thái dở dang
                .doOnError(e -> tokenManager.clearTokens())

                // 7. Chuyển đổi lỗi thân thiện cho UI
                .compose(ApiErrorMapper.forSingle("Số điện thoại hoặc mật khẩu không chính xác"));
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
                        // userRepository.clearUserSession();
                    }));
        });
    }

    @Override
    public Completable sendOtp(String phoneNumber, OtpPurpose purpose) {
        if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
            return Completable.error(new IllegalArgumentException("Số điện thoại không hợp lệ"));
        }
        SendOtpRequest request = new SendOtpRequest(phoneNumber);
        Single<SendOtpResponse> call = purpose == OtpPurpose.REGISTER
                ? authApi.sendRegisterOtp(request)
                : authApi.sendForgotPasswordOtp(request);
        return call.ignoreElement().compose(ApiErrorMapper.forCompletable("Gửi OTP thất bại"));
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
    public Completable resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Token xác thực không hợp lệ"));
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Completable.error(new IllegalArgumentException("Mật khẩu phải chứa ít nhất 6 ký tự"));
        }
        return authApi.resetPassword(new ResetPasswordRequest(resetToken, newPassword))
                .compose(ApiErrorMapper.forCompletable("Đặt lại mật khẩu thất bại"));
    }

    private String extractRoleFromToken(String token) {
        if (token == null) return null;
        // MOCK LOGIC: Sau này thay bằng JWT Decoder thật
        if (token.contains("manager")) return "MANAGER";
        if (token.contains("employee")) return "EMPLOYEE";
        return null;
    }
}