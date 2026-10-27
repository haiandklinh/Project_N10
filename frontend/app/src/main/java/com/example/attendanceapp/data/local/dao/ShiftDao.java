package com.example.attendanceapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.attendanceapp.data.local.entity.ShiftEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertShift(ShiftEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertShifts(List<ShiftEntity> entities);

    @Update
    Completable updateShift(ShiftEntity entity);

    @Query("DELETE FROM shifts WHERE id = :shiftId")
    Completable deleteShift(String shiftId);

    @Query("DELETE FROM shifts")
    Completable clearAll();

    @Query("SELECT * FROM shifts")
    Flowable<List<ShiftEntity>> observeAllShifts();

    @Query("SELECT * FROM shifts WHERE id = :shiftId")
    Flowable<ShiftEntity> observeShiftById(String shiftId);
}
