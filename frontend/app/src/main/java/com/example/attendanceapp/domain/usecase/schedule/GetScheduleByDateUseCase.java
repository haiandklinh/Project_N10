package com.example.attendanceapp.domain.usecase.schedule;

import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetScheduleByDateUseCase {
    private final ScheduleRepository scheduleRepository;

    @Inject
    public GetScheduleByDateUseCase(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Single<List<StaffWorkScheduleResponse>> execute(String date) {
        return scheduleRepository.getStaffWorkSchedules(date);
    }
}
