package com.example.attendanceapp.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.data.dto.request.auth.RefreshTokenRequest;
import com.example.attendanceapp.data.dto.response.auth.RefreshTokenResponse;
import com.example.attendanceapp.data.local.TokenManager;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

@Singleton
public class TokenAuthenticator implements Authenticator {

    private final TokenManager tokenManager;
    // Sử dụng Provider<AuthApi> để tránh vòng lặp phụ thuộc (Circular Dependency)
    private final Provider<AuthApi> authApiProvider;

    @Inject
    public TokenAuthenticator(TokenManager tokenManager, Provider<AuthApi> authApiProvider) {
        this.tokenManager = tokenManager;
        this.authApiProvider = authApiProvider;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // 1. Nếu request đã bị từ chối 401 từ 2 lần trở lên -> Bỏ qua để tránh lặp vô hạn
        if (responseCount(response) >= 2) {
            return null;
        }

        // Bỏ qua nếu lỗi 401 là do API join-requests hoặc do nghiệp vụ Backend (ví dụ: Staff is in company) chứ không phải do Token hết hạn
        try {
            if (response.request().url().encodedPath().contains("join-requests")) {
                return null;
            }
            if (response.body() != null) {
                String errorBody = response.peekBody(1024).string();
                if (errorBody != null) {
                    String lower = errorBody.toLowerCase();
                    if (lower.contains("staff is in company") || lower.contains("in company") || lower.contains("not found manager")) {
                        return null;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Đồng bộ hóa để tránh nhiều API đồng thời cùng đi Refresh Token một lúc
        synchronized (this) {
            String currentAccessToken = tokenManager.getAccessToken();
            String requestToken = response.request().header("Authorization");

            // Nếu Access Token hiện tại trong TokenManager đã khác với Token của Request bị lỗi,
            // nghĩa là có một luồng khác đã thực hiện Refresh Token thành công trước đó rồi.
            if (requestToken != null && !requestToken.equals("Bearer " + currentAccessToken)) {
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + currentAccessToken)
                        .build();
            }

            String refreshToken = tokenManager.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                tokenManager.clearTokens();
                return null;
            }

            // 2. Lấy AuthApi từ Provider và gọi API Refresh Token đồng bộ
            AuthApi authApi = authApiProvider.get();
            Call<RefreshTokenResponse> call = authApi.refreshToken(new RefreshTokenRequest(refreshToken));

            retrofit2.Response<RefreshTokenResponse> retrofitResponse = call.execute();

            if (retrofitResponse.isSuccessful() && retrofitResponse.body() != null) {
                RefreshTokenResponse newTokenData = retrofitResponse.body();
                
                String nextRefreshToken = (newTokenData.getRefreshToken() != null && !newTokenData.getRefreshToken().isEmpty()) 
                        ? newTokenData.getRefreshToken() 
                        : refreshToken;

                // Lưu cặp token mới vào bộ nhớ cục bộ
                tokenManager.saveTokens(newTokenData.getAccessToken(), nextRefreshToken);

                // Tạo lại Request bị lỗi trước đó với Access Token mới
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newTokenData.getAccessToken())
                        .build();
            } else {
                // Refresh Token hết hạn hoặc không hợp lệ -> Xóa Token và yêu cầu đăng nhập lại
                tokenManager.clearTokens();
                return null;
            }
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
