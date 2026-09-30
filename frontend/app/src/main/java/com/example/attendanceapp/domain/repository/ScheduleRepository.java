package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface ScheduleRepository {

    // ===== Dành cho Quản lý =====

    /** Lấy toàn bộ danh sách nhân viên và lịch làm việc */
    Single<List<EmployeeWorkSchedule>> getEmployeeWorkSchedules();

    /** Xếp ca làm và lịch làm cho nhân viên */
    Single<EmployeeWorkSchedule> assignWorkSchedule(String employeeId, String name, String position,
                                                      String shiftId, String workDate);

    // ===== Dành cho Nhân viên =====

    /** Lấy ra lịch làm việc của nhân viên theo ngày */
    Single<List<StaffWorkScheduleResponse>> getStaffWorkSchedules(String date);
}
