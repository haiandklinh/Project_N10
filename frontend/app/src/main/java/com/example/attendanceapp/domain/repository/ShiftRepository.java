package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.ShiftOption;
import java.util.List;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ShiftRepository {
    Single<List<ShiftOption>> getShifts();
    Single<ShiftOption> getShiftDetail(String shiftId);
    Single<ShiftOption> addShift(String shiftName, String startTime, String endTime, String description);
    Single<ShiftOption> updateShift(String shiftId, String shiftName, String startTime, String endTime, String description);
    Completable deleteShift(String shiftId);
}