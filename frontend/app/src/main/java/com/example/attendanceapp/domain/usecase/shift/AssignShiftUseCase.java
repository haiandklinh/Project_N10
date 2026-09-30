package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class AssignShiftUseCase {
    private final ScheduleRepository scheduleRepository;

    @Inject
    public AssignShiftUseCase(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Single<EmployeeWorkSchedule> execute(String employeeId, String name, String position,
                                                  String shiftId, String workDate) {
        return scheduleRepository.assignWorkSchedule(employeeId, name, position, shiftId, workDate);
    }
}
