package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;

public class ObserveShiftsUseCase {

    private final ShiftRepository shiftRepository;

    @Inject
    public ObserveShiftsUseCase(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Observable<List<ShiftOption>> execute() {
        return shiftRepository.observeShifts();
    }
}