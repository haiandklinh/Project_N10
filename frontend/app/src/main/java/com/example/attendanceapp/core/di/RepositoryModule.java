// di/RepositoryModule.java
package com.example.attendanceapp.core.di;

import com.example.attendanceapp.data.repository.AppStateRepositoryImpl;
import com.example.attendanceapp.data.repository.AuthRepositoryImpl;
import com.example.attendanceapp.data.repository.SessionRepositoryImpl;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;


/**
 * Bắt buộc phải có module này: @Inject constructor trên AuthRepositoryImpl
 * chỉ giúp Hilt biết cách TẠO AuthRepositoryImpl, chứ không tự động ánh xạ
 * interface AuthRepository -> AuthRepositoryImpl khi có nơi khác (ViewModel)
 * inject theo kiểu interface.
 *
 * Dùng @Binds (thay vì @Provides) vì đây chỉ là ánh xạ interface -> impl,
 * không cần logic khởi tạo phức tạp - Dagger/Hilt sẽ hiệu quả hơn @Provides.
 */

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract AppStateRepository bindAppStateRepository(AppStateRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract SessionRepository bindUserRepository(SessionRepositoryImpl sessionRepositoryImpl);
}