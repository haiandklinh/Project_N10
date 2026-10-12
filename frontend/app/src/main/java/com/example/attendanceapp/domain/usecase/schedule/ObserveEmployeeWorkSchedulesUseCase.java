package com.example.attendanceapp.domain.usecase.schedule;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;

public class ObserveEmployeeWorkSchedulesUseCase {

    private final ScheduleRepository scheduleRepository;

    @Inject
    public ObserveEmployeeWorkSchedulesUseCase(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Observable<List<EmployeeWorkSchedule>> execute() {
        return scheduleRepository.observeEmployeeWorkSchedules();
    }
}