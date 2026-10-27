package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.attendanceapp.data.local.entity.WorkScheduleEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface WorkScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdate(WorkScheduleEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdateSchedules(List<WorkScheduleEntity> entities);

    @Query("SELECT * FROM work_schedules WHERE workDate = :workDate")
    Flowable<List<WorkScheduleEntity>> observeSchedulesByDate(String workDate);

    @Query("SELECT * FROM work_schedules")
    Flowable<List<WorkScheduleEntity>> observeAllSchedules();
    
    @Query("DELETE FROM work_schedules WHERE employeeId = :employeeId AND workDate = :workDate")
    Completable deleteSchedule(String employeeId, String workDate);

    @Query("DELETE FROM work_schedules")
    Completable clearAll();
}
