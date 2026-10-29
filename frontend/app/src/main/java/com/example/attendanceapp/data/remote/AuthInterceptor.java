package com.example.attendanceapp.data.remote;

import androidx.annotation.NonNull;

import com.example.attendanceapp.data.local.TokenManager;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//AuthInterceptor.java là file tự động đính kèm Access Token vào các request HTTP để backend biết người dùng nào đang gọi API.
@Singleton
public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    @Inject
    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        //Lấy request ban đầu và token
        Request originalRequest = chain.request();
        String token = tokenManager.getAccessToken();

        // Nếu không có token (chưa đăng nhập / API public như login, register), gửi request gốc
        if (token == null || token.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        //Nếu có token thì thêm header
        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authenticatedRequest);
    }
}