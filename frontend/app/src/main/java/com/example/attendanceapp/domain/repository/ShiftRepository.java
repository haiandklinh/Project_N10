package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.ShiftOption;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface ShiftRepository {

    /** Gọi API lấy danh sách ca và đẩy kết quả vào stream observeShifts(). */
    Single<List<ShiftOption>> getShifts();

    /** Stream danh sách ca dùng chung. Mọi thay đổi (tải, thêm, sửa, xóa) đều được phát ra ở đây. */
    Observable<List<ShiftOption>> observeShifts();

    Single<ShiftOption> getShiftDetail(String shiftId);

    Single<ShiftOption> addShift(String shiftName, String startTime, String endTime, Double latitude, Double longitude);

    Single<ShiftOption> updateShift(String shiftId, String shiftName, String startTime, String endTime, Double latitude, Double longitude);

    Completable deleteShift(String shiftId);
}