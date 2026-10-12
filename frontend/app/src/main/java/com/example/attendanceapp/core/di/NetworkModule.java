package com.example.attendanceapp.core.di;

import android.content.Context;

import com.example.attendanceapp.BuildConfig;
import com.example.attendanceapp.data.remote.AuthApi;
import com.example.attendanceapp.data.remote.AuthInterceptor;
import com.example.attendanceapp.data.remote.MockInterceptor;
import com.example.attendanceapp.data.remote.ScheduleApi;
import com.example.attendanceapp.data.remote.ShiftApi;
import com.example.attendanceapp.data.remote.TokenAuthenticator;
import com.example.attendanceapp.data.remote.UserApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Module cung cấp toàn bộ tầng network cho ứng dụng.
 * Thay thế cho ApiClient.java (singleton thủ công) trước đây.
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // Chỉ log BODY ở bản debug để tránh lộ password/OTP/token ra Logcat ở bản release
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
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
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(tokenAuthenticator);

        // TODO: Comment khi chạy production , Uncomment khi test mock,
        builder.addInterceptor(mockInterceptor);

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public AuthApi provideApiService(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }
    @Provides
    @Singleton
    public UserApi provideUserApi(Retrofit retrofit) {
        return retrofit.create(UserApi.class);
    }

    @Provides
    @Singleton
    public ScheduleApi provideScheduleApi(Retrofit retrofit) {
        return retrofit.create(ScheduleApi.class);
    }
    @Provides
    @Singleton
    public ShiftApi provideShiftApi(Retrofit retrofit) {
        return retrofit.create(ShiftApi.class);
    }
}