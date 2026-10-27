// di/RepositoryModule.java
package com.example.attendanceapp.core.di;

import com.example.attendanceapp.data.repository.AppStateRepositoryImpl;
import com.example.attendanceapp.data.repository.AttendanceRepositoryImpl;
import com.example.attendanceapp.data.repository.AuthRepositoryImpl;
import com.example.attendanceapp.data.repository.ManagerRepositoryImpl;
import com.example.attendanceapp.data.repository.NotificationRepositoryImpl;
import com.example.attendanceapp.data.repository.ScheduleRepositoryImpl;
import com.example.attendanceapp.data.repository.SessionRepositoryImpl;
import com.example.attendanceapp.data.repository.ShiftRepositoryImpl;
import com.example.attendanceapp.data.repository.StaffRepositoryImpl;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.example.attendanceapp.domain.repository.AttendanceRepository;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.repository.ScheduleRepository;
import com.example.attendanceapp.domain.repository.SessionRepository;
import com.example.attendanceapp.domain.repository.ShiftRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;

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
    public abstract SessionRepository bindSessionRepository(SessionRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract ScheduleRepository bindScheduleRepository(ScheduleRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract ShiftRepository bindShiftRepository(ShiftRepositoryImpl shiftRepositoryImpl);

    @Binds
    @Singleton
    public abstract AttendanceRepository bindAttendanceRepository(
            AttendanceRepositoryImpl attendanceRepositoryImpl
    );

    @Binds
    @Singleton
    public abstract NotificationRepository bindNotificationRepository(NotificationRepositoryImpl impl);

    @Binds
    public abstract StaffRepository bindStaffRepository(StaffRepositoryImpl impl);

    @Binds
    public abstract ManagerRepository bindManagerRepository(ManagerRepositoryImpl impl);
}
