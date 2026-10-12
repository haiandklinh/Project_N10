package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class AssignShiftUseCase {
    private final ScheduleRepository scheduleRepository;

    @Inject
    public AssignShiftUseCase(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Single<List<EmployeeWorkSchedule>> execute(List<String> employeeIds, String shiftId, String workDate, String note) {
        return scheduleRepository.assignWorkSchedule(employeeIds, shiftId, workDate, note);
    }
}
