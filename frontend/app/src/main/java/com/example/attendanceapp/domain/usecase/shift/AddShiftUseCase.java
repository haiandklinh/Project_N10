package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class AddShiftUseCase {

    private final ShiftRepository shiftRepository;

    @Inject
    public AddShiftUseCase(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Single<ShiftOption> execute(String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        return shiftRepository.addShift(shiftName, startTime, endTime, latitude, longitude);
    }
}