package com.example.attendanceapp.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.data.dto.request.RefreshTokenRequest;
import com.example.attendanceapp.data.dto.response.RefreshTokenResponse;
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
    // Provider<ApiService> phá vỡ vòng lặp phụ thuộc:
    // ApiService -> Retrofit -> OkHttpClient -> Authenticator -> ApiService.
    // Hilt tự cung cấp Provider này, không cần code thủ công.
    private final Provider<ApiService> apiServiceProvider;

    @Inject
    public TokenAuthenticator(TokenManager tokenManager, Provider<ApiService> apiServiceProvider) {
        this.tokenManager = tokenManager;
        this.apiServiceProvider = apiServiceProvider;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // 1. Nếu request đã bị từ chối 401 từ 2 lần trở lên -> Bỏ qua để tránh lặp vô hạn
        if (responseCount(response) >= 2) {
            return null;
        }

        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            tokenManager.clearTokens();
            return null;
        }

        // 2. Lấy ApiService từ Provider và gọi API Refresh Token đồng bộ
        ApiService apiService = apiServiceProvider.get();
        Call<RefreshTokenResponse> call =
                apiService.refreshToken(new RefreshTokenRequest(refreshToken));

        retrofit2.Response<RefreshTokenResponse> retrofitResponse = call.execute();

        if (retrofitResponse.isSuccessful() && retrofitResponse.body() != null) {

            RefreshTokenResponse newTokenData = retrofitResponse.body();

            // Lưu Access Token mới, giữ nguyên Refresh Token cũ
            tokenManager.saveTokens(
                    newTokenData.getAccessToken(),
                    refreshToken
            );

            // Tạo lại Request bị lỗi trước đó với Access Token mới
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newTokenData.getAccessToken())
                    .build();
        } else {
            // Refresh Token hết hạn hoặc không hợp lệ -> Xóa Token (Yêu cầu đăng nhập lại)
            tokenManager.clearTokens();
            return null;
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