package com.example.attendanceapp.domain.usecase.shift;

import com.example.attendanceapp.domain.repository.ShiftRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class DeleteShiftUseCase {

    private final ShiftRepository repository;

    @Inject
    public DeleteShiftUseCase(ShiftRepository repository) {
        this.repository = repository;
    }

    public Completable execute(String shiftId) {
        return repository.deleteShift(shiftId);
    }
}