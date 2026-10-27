package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.ShiftOption;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface ShiftRepository {

    /** Phát lại danh sách ca từ Room mỗi khi có thay đổi. */
    Observable<List<ShiftOption>> observeShifts();

    /** Tải từ server, lưu vào Room. */
    Single<List<ShiftOption>> getShifts();

    Single<ShiftOption> getShiftDetail(String shiftId);

    Single<ShiftOption> addShift(String shiftName, String startTime, String endTime,
                                 String allowedCheckInTime, double checkInRadius,
                                 double latitude, double longitude);

    Single<ShiftOption> updateShift(String shiftId, String shiftName, String startTime, String endTime,
                                    String allowedCheckInTime, double checkInRadius,
                                    double latitude, double longitude);

    Completable deleteShift(String shiftId);
}