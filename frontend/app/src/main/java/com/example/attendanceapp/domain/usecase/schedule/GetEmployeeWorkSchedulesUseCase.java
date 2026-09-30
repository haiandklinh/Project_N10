package com.example.attendanceapp.domain.usecase.schedule;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetEmployeeWorkSchedulesUseCase {
    private final ScheduleRepository scheduleRepository;

    @Inject
    public GetEmployeeWorkSchedulesUseCase(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Single<List<EmployeeWorkSchedule>> execute() {
        return scheduleRepository.getEmployeeWorkSchedules();
    }
}
