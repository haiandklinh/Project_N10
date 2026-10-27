package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/** Lịch làm việc của nhân viên do Quản lý xem/xếp (ScheduleApi). Lịch của chính Staff nằm ở StaffRepository. */
public interface ScheduleRepository {

    /** Phát lại danh sách từ Room mỗi khi có thay đổi. */
    Observable<List<EmployeeWorkSchedule>> observeEmployeeWorkSchedules();

    /** Tải từ server, lưu vào Room, trả danh sách mới. */
    Single<List<EmployeeWorkSchedule>> getEmployeeWorkSchedules();

    /**
     * Xếp ca cho nhiều nhân viên trong 1 ngày. Server nhận từng nhân viên một nên repo gọi lần lượt,
     * xong thì tải lại danh sách (Room cập nhật, các ViewModel đang observe tự nhận).
     */
    Completable assignWorkSchedule(List<String> employeeIds, List<String> shiftIds, String workDate);
}