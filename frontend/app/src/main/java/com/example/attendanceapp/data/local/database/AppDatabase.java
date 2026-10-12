package com.example.attendanceapp.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.attendanceapp.data.local.dao.AttendanceDao;
import com.example.attendanceapp.data.local.dao.ShiftDao;
import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.dao.WorkScheduleDao;
import com.example.attendanceapp.data.local.entity.ShiftEntity;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.data.local.entity.WorkScheduleEntity;

@Database(entities = {UserEntity.class, ShiftEntity.class, WorkScheduleEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ShiftDao shiftDao();
    public abstract WorkScheduleDao workScheduleDao();
}