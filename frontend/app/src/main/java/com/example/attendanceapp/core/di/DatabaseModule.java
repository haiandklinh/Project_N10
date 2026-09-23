package com.example.attendanceapp.core.di;

import android.content.Context;

import androidx.room.Room;


import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.database.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        "attendance_app_db"
                )
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public static UserDao provideUserDao(AppDatabase appDatabase) {
        return appDatabase.userDao();
    }
}