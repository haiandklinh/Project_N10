package com.example.attendanceapp.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.entity.UserEntity;

@Database(entities = {UserEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}