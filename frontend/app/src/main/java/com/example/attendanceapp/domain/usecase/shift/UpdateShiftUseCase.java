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

    public Single<ShiftOption> execute(String shiftId, String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        double lat = latitude != null ? latitude : 0.0;
        double lon = longitude != null ? longitude : 0.0;
        String allowedCheckIn = startTime != null ? startTime : "00:00";
        double radius = 100.0;
        return shiftRepository.updateShift(shiftId, shiftName, startTime, endTime, allowedCheckIn, radius, lat, lon);
    }

    public Single<ShiftOption> execute(String shiftId, String shiftName, String startTime, String endTime,
                                       String allowedCheckInTime, double checkInRadius,
                                       double latitude, double longitude) {
        return shiftRepository.updateShift(shiftId, shiftName, startTime, endTime, allowedCheckInTime, checkInRadius, latitude, longitude);
    }
}
