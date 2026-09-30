package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

//Lấy chi tiết thông tin của 1 ca làm việc theo shiftId.
public class GetShiftDetailUseCase {

    private final ShiftRepository shiftRepository;

    @Inject
    public GetShiftDetailUseCase(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Single<ShiftOption> execute(String shiftId) {
        return shiftRepository.getShiftDetail(shiftId);
    }
}