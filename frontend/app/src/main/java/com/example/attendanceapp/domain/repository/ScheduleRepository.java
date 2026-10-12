package com.example.attendanceapp.domain.repository;

import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface ScheduleRepository {

    /** Gọi API lấy lịch làm việc và đẩy kết quả vào stream observeEmployeeWorkSchedules(). */
    Single<List<EmployeeWorkSchedule>> getEmployeeWorkSchedules();

    /** Stream lịch làm việc dùng chung giữa các ViewModel. */
    Observable<List<EmployeeWorkSchedule>> observeEmployeeWorkSchedules();

    Single<List<EmployeeWorkSchedule>> assignWorkSchedule(List<String> employeeIds, String shiftId, String workDate, String note);

    // TODO: nên map sang domain model thay vì trả DTO của tầng data
    Single<List<StaffWorkScheduleResponse>> getStaffWorkSchedules(String date);
}