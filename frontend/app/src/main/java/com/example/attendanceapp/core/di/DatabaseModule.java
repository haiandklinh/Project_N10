package com.example.attendanceapp.core.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.database.AppDatabase;
import com.example.attendanceapp.data.local.entity.UserEntity;

import java.util.concurrent.Executors;

import javax.inject.Provider;
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
    public static AppDatabase provideAppDatabase(
            @ApplicationContext Context context,
            Provider<UserDao> userDaoProvider
    ) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        "attendance_app_db"
                )
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        // Tạo sẵn 2 tài khoản mock ngay khi tạo DB
                        Executors.newSingleThreadExecutor().execute(() -> {
                            UserDao dao = userDaoProvider.get();
                            
                            // 1. Quản lý
                            UserEntity manager = new UserEntity("mgr_001", "Quản Lý Mock", "0961474876", "MANAGER", "APPROVED");
                            manager.setFirstLogin(false); // Quan trọng: Đã có vai trò
                            dao.insertOrUpdateUser(manager);

                            // 2. Nhân viên
                            UserEntity employee = new UserEntity("emp_001", "Nhân Viên Mock", "0366494300", "EMPLOYEE", "APPROVED");
                            employee.setFirstLogin(false); // Quan trọng: Đã có vai trò
                            dao.insertOrUpdateUser(employee);
                        });
                    }
                })
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public static UserDao provideUserDao(AppDatabase appDatabase) {
        return appDatabase.userDao();
    }
}