package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;
import com.example.attendanceapp.data.local.entity.AttendanceRecordEntity;
import io.reactivex.rxjava3.core.Flowable; // Hoặc dùng Coroutines Flow/LiveData

@Dao
public interface AttendanceDao {
    @Query("SELECT * FROM AttendanceRecordEntity WHERE date = :date LIMIT 1")
    Flowable<AttendanceRecordEntity> getAttendanceByDate(String date);

    //TODO: Các câu lệnh insert/update...
}