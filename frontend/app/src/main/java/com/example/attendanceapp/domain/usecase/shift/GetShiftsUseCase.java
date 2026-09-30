package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;


//Lấy danh sách các ca làm việc.
public class GetShiftsUseCase {

    private final ShiftRepository shiftRepository;

    @Inject
    public GetShiftsUseCase(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Single<List<ShiftOption>> execute() {
        return shiftRepository.getShifts();
    }
}