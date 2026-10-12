package com.example.attendanceapp.core.di;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.attendanceapp.BuildConfig;
import com.example.attendanceapp.data.local.dao.ShiftDao;
import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.dao.WorkScheduleDao;
import com.example.attendanceapp.data.local.database.AppDatabase;
import com.example.attendanceapp.data.local.database.Migrations;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    private static final String DATABASE_NAME = "attendance_app_db";

    private DatabaseModule() {
    }

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME);

        RoomDatabase.Builder<AppDatabase> builder =
                Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                        .addMigrations(Migrations.ALL);

        // Chỉ cho phép xóa DB khi debug; release bắt buộc phải có Migration.
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration();
        }

        return builder.build();
    }

    @Provides
    public static UserDao provideUserDao(AppDatabase db) {
        return db.userDao();
    }

    @Provides
    public static ShiftDao provideShiftDao(AppDatabase db) {
        return db.shiftDao();
    }

    @Provides
    public static WorkScheduleDao provideWorkScheduleDao(AppDatabase db) {
        return db.workScheduleDao();
    }
}