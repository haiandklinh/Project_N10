package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class UpdateShiftUseCase {

    private final ShiftRepository shiftRepository;

    @Inject
    public UpdateShiftUseCase(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Single<ShiftOption> execute(String shiftId, String shiftName, String startTime, String endTime, String description) {
        return shiftRepository.updateShift(shiftId, shiftName, startTime, endTime, description);
    }
}