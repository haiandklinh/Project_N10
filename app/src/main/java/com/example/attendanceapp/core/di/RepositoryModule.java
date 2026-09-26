// di/RepositoryModule.java
package com.example.attendanceapp.core.di;

import com.example.attendanceapp.data.repository.AppStateRepositoryImpl;
import com.example.attendanceapp.data.repository.AuthRepositoryImpl;
import com.example.attendanceapp.data.repository.UserRepositoryImpl;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.UserRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

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
    public abstract UserRepository bindUserRepository(UserRepositoryImpl userRepositoryImpl);
}