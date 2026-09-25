package com.example.attendanceapp.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.attendanceapp.data.dto.request.CheckPhoneRequest;
import com.example.attendanceapp.data.dto.request.LoginRequest;
import com.example.attendanceapp.data.dto.request.ProfileRequest;
import com.example.attendanceapp.data.dto.request.RefreshTokenRequest;
import com.example.attendanceapp.data.dto.request.RegisterRequest;
import com.example.attendanceapp.data.dto.request.ResetPasswordRequest;
import com.example.attendanceapp.data.dto.request.SendOtpRequest;
import com.example.attendanceapp.data.dto.request.VerifyOtpRequest;
import com.example.attendanceapp.data.dto.response.AuthResponse;
import com.example.attendanceapp.data.dto.response.CheckPhoneResponse;
import com.example.attendanceapp.data.dto.response.MessageResponse;
import com.example.attendanceapp.data.dto.response.ProfileResponse;
import com.example.attendanceapp.data.dto.response.SendOtpResponse;
import com.example.attendanceapp.data.dto.response.VerifyOtpResponse;
import com.example.attendanceapp.data.local.TokenManager;
import com.example.attendanceapp.data.remote.ApiService;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final SessionRepository sessionRepository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    @Inject
    public AuthRepositoryImpl(ApiService apiService, TokenManager tokenManager, SessionRepository sessionRepository) {
        this.apiService = apiService;
        this.tokenManager = tokenManager;
        this.sessionRepository = sessionRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void register(String phoneNumber, String password, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                RegisterRequest request = new RegisterRequest(phoneNumber, password);
                Call<MessageResponse> call = apiService.register(request);
                Response<MessageResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    postSuccess(callback, null);
                } else {
                    postError(callback, resolveErrorMessage(response, "Đăng ký thất bại"));
                }
            } catch (IOException e) {
                postError(callback, "Lỗi mạng: " + e.getMessage());
            } catch (Exception e) {
                postError(callback, "Lỗi đăng ký: " + e.getMessage());
            }
        });
    }

    @Override
    public void checkPhoneExists(String phoneNumber, DataCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                CheckPhoneRequest request = new CheckPhoneRequest(phoneNumber);
                Call<CheckPhoneResponse> call = apiService.checkPhone(request);
                Response<CheckPhoneResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    postSuccess(callback, response.body().isExist());
                } else {
                    postError(callback, resolveErrorMessage(response, "Kiểm tra số điện thoại thất bại"));
                }
            } catch (IOException e) {
                postError(callback, "Lỗi mạng: " + e.getMessage());
            } catch (Exception e) {
                postError(callback, "Lỗi kiểm tra số điện thoại: " + e.getMessage());
            }
        });
    }

    @Override
    public void login(String phoneNumber, String password, DataCallback<UserAccount> callback) {
        executorService.execute(() -> {
            try {
                LoginRequest request = new LoginRequest(phoneNumber, password);
                Call<AuthResponse> call = apiService.login(request);
                Response<AuthResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    tokenManager.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());

                    // TODO: Backend chưa có GET /profile để lấy thông tin user sau khi login.
                    // Không gọi lại POST /profile ở đây vì đó là API TẠO MỚI, gọi lại có nguy cơ
                    // tạo trùng/ghi đè profile đã có. Tạm thời tạo UserAccount tối thiểu.
                    UserAccount minimalAccount = new UserAccount(
                            null, null, phoneNumber, null, null, null, null,
                            true, true, Instant.now(), UserStatus.NEEDS_ROLE_SELECTION
                    );
                    mainHandler.post(() -> {
                        sessionRepository.saveUserSession(minimalAccount, true);
                        sessionRepository.saveUserStatus(UserStatus.NEEDS_ROLE_SELECTION);
                        callback.onSuccess(minimalAccount);
                    });
                } else {
                    postError(callback, resolveErrorMessage(response, "Số điện thoại hoặc mật khẩu không chính xác"));
                }
            } catch (IOException e) {
                postError(callback, "Lỗi mạng: " + e.getMessage());
            } catch (Exception e) {
                postError(callback, "Lỗi đăng nhập: " + e.getMessage());
            }
        });
    }

    @Override
    public void selectRole(UserRole role, DataCallback<Void> callback) {
        executorService.execute(() -> mainHandler.post(() -> {
            sessionRepository.saveUserRole(role);
            postSuccess(callback, null);
        }));
    }

    @Override
    public void updatePersonalInfo(String name, String dob, String gender, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                ProfileRequest request = new ProfileRequest();
                request.setFullName(name);
                request.setDate(dob);
                request.setGender(gender);

                Call<ProfileResponse> call = apiService.createProfile(request);
                Response<ProfileResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    UserAccount currentUser = sessionRepository.getCurrentUser();
                    if (currentUser != null) {
                        java.time.LocalDate localDate = null;
                        try {
                            localDate = java.time.LocalDate.parse(dob);
                        } catch (Exception ignored) {}

                        UserAccount updated = currentUser.withPersonalInfo(name, localDate, gender);
                        mainHandler.post(() -> {
                            sessionRepository.saveUserSession(updated, true);
                            postSuccess(callback, null);
                        });
                    } else {
                        postSuccess(callback, null);
                    }
                } else {
                    postError(callback, resolveErrorMessage(response, "Cập nhật thông tin thất bại"));
                }
            } catch (Exception e) {
                postError(callback, "Lỗi cập nhật thông tin cá nhân: " + e.getMessage());
            }
        });
    }

    @Override
    public void createCompany(String companyName, DataCallback<String> callback) {
        // TODO: ProfileRequest/ApiService hiện KHÔNG có field/endpoint cho công ty.
        postError(callback, "Chức năng tạo công ty chưa được backend hỗ trợ (thiếu endpoint).");
    }

    @Override
    public void joinCompanyWithCode(String invitationCode, DataCallback<String> callback) {
        // TODO: cần endpoint POST /company/join từ backend.
        postError(callback, "Chức năng tham gia công ty chưa được backend hỗ trợ (thiếu endpoint).");
    }

    @Override
    public void sendOtp(String phoneNumber, OtpPurpose purpose, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
                    postError(callback, "Số điện thoại không hợp lệ");
                    return;
                }

                SendOtpRequest request = new SendOtpRequest(phoneNumber);
                Call<SendOtpResponse> call;

                if (purpose == OtpPurpose.REGISTER) {
                    call = apiService.sendRegisterOtp(request);
                } else {
                    call = apiService.sendForgotPasswordOtp(request);
                }

                Response<SendOtpResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    postSuccess(callback, null);
                } else {
                    postError(callback, resolveErrorMessage(response, "Gửi OTP thất bại"));
                }
            } catch (IOException e) {
                postError(callback, "Lỗi mạng: " + e.getMessage());
            } catch (Exception e) {
                postError(callback, "Lỗi gửi OTP: " + e.getMessage());
            }
        });
    }

    @Override
    public void verifyOtp(String phoneNumber, String otp, OtpPurpose purpose, DataCallback<String> callback) {
        executorService.execute(() -> {
            try {
                VerifyOtpRequest request = new VerifyOtpRequest(phoneNumber, otp);
                Call<VerifyOtpResponse> call;

                if (purpose == OtpPurpose.REGISTER) {
                    call = apiService.verifyRegisterOtp(request);
                } else {
                    call = apiService.verifyForgotPasswordOtp(request);
                }

                Response<VerifyOtpResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null && response.body().isVerified()) {
                    postSuccess(callback, phoneNumber);
                } else {
                    postError(callback, resolveErrorMessage(response, "Mã OTP không đúng hoặc đã hết hạn"));
                }
            } catch (IOException e) {
                postError(callback, "Lỗi mạng: " + e.getMessage());
            } catch (Exception e) {
                postError(callback, "Lỗi xác thực OTP: " + e.getMessage());
            }
        });
    }

    @Override
    public void resetPassword(String phone, String newPassword, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                if (newPassword == null || newPassword.length() < 6) {
                    postError(callback, "Mật khẩu phải chứa ít nhất 6 ký tự");
                    return;
                }

                ResetPasswordRequest request = new ResetPasswordRequest(phone, newPassword);
                Call<MessageResponse> call = apiService.resetPassword(request);
                Response<MessageResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    postSuccess(callback, null);
                } else {
                    postError(callback, resolveErrorMessage(response, "Đặt lại mật khẩu thất bại"));
                }
            } catch (IOException e) {
                postError(callback, "Lỗi mạng: " + e.getMessage());
            } catch (Exception e) {
                postError(callback, "Lỗi đặt lại mật khẩu: " + e.getMessage());
            }
        });
    }

    @Override
    public void logout(DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                String refreshToken = tokenManager.getRefreshToken();
                if (refreshToken != null) {
                    RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
                    Call<MessageResponse> call = apiService.logout(request);
                    call.execute(); // Dù server trả lỗi vẫn clear session local phía dưới
                }
            } catch (Exception ignored) {
                // Không chặn luồng logout local dù API lỗi/mất mạng
            } finally {
                tokenManager.clearTokens();
                sessionRepository.clearUserSession();
                postSuccess(callback, null);
            }
        });
    }

    /**
     * Đọc message lỗi từ errorBody khi response HTTP thất bại.
     * BE dùng FastAPI nên có 2 dạng lỗi cần xử lý:
     * 1) Lỗi validate tự sinh (422): { "detail": [ { "msg": "...", "loc": [...], ... } ] }
     * 2) Lỗi nghiệp vụ tự viết (401...): CẦN XÁC NHẬN LẠI VỚI BE FORMAT THẬT,
     *    tạm thời thử đọc "detail" dạng String đơn giản hoặc "message" để dự phòng.
     */
    private String resolveErrorMessage(Response<?> response, String defaultError) {
        try {
            if (response.errorBody() == null) return defaultError;
            String errorJson = response.errorBody().string();
            JSONObject jsonObject = new JSONObject(errorJson);

            Object detail = jsonObject.opt("detail");

            // Dạng 1: detail là mảng lỗi validate (422 chuẩn FastAPI)
            if (detail instanceof JSONArray) {
                JSONArray detailArray = (JSONArray) detail;
                if (detailArray.length() > 0) {
                    JSONObject firstError = detailArray.getJSONObject(0);
                    if (firstError.has("msg")) {
                        return firstError.getString("msg");
                    }
                }
            }
            // Dạng 2: detail là String đơn giản (một số API FastAPI tự raise HTTPException(detail="..."))
            else if (detail instanceof String) {
                return (String) detail;
            }

            // Dạng 3: dự phòng nếu BE có custom field "message" ở vài endpoint khác
            if (jsonObject.has("message") && !jsonObject.isNull("message")) {
                return jsonObject.getString("message");
            }
        } catch (Exception ignored) {}
        return defaultError;
    }

    private <T> void postSuccess(DataCallback<T> callback, T result) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(DataCallback<T> callback, String message) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(message));
        }
    }
}