package com.example.attendanceapp.core.di;

import android.content.Context;

import com.example.attendanceapp.data.remote.ApiService;
import com.example.attendanceapp.data.remote.AuthInterceptor;
import com.example.attendanceapp.data.remote.MockInterceptor;
import com.example.attendanceapp.data.remote.TokenAuthenticator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Module cung cấp toàn bộ tầng network cho ứng dụng.
 * Thay thế cho ApiClient.java (singleton thủ công) trước đây.
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // TODO: đổi thành URL API thật của bạn
    private static final String BASE_URL = "https://your-api-domain.com/api/";

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // Chỉ log BODY ở bản debug để tránh lộ password/OTP/token ra Logcat ở bản release.
        // Nếu chưa dùng BuildConfig của app module ở đây, có thể truyền cờ BuildConfig.DEBUG
        // từ module app xuống qua một class wrapper, hoặc set trực tiếp:
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // TODO: gate theo BuildConfig.DEBUG
        return logging;
    }

    //Mock
    @Provides
    @Singleton
    public MockInterceptor provideMockInterceptor(@ApplicationContext Context context) {
        return new MockInterceptor(context);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(
            AuthInterceptor authInterceptor,
            MockInterceptor mockInterceptor,  //Mock
            TokenAuthenticator tokenAuthenticator,
            HttpLoggingInterceptor loggingInterceptor
    ) {
        return new OkHttpClient.Builder()
                .addInterceptor(mockInterceptor) // Thêm dòng này để mock
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(tokenAuthenticator)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}