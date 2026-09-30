package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.work.AssignWorkScheduleRequest;
import com.example.attendanceapp.data.dto.response.schedule.EmployeeWorkScheduleResponse;
import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.ScheduleApi;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

@Singleton
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final ScheduleApi scheduleApi;

    @Inject
    public ScheduleRepositoryImpl(ScheduleApi scheduleApi) {
        this.scheduleApi = scheduleApi;
    }

    @Override
    public Single<List<EmployeeWorkSchedule>> getEmployeeWorkSchedules() {
        return scheduleApi.getEmployeeWorkSchedules()
                .map(this::mapToDomainList)
                .compose(ApiErrorMapper.forSingle("Lấy danh sách lịch làm việc thất bại"));
    }

    @Override
    public Single<EmployeeWorkSchedule> assignWorkSchedule(String employeeId, String name, String position,
                                                              String shiftId, String workDate) {
        AssignWorkScheduleRequest request = new AssignWorkScheduleRequest(
                employeeId, name, position, shiftId, workDate
        );
        return scheduleApi.assignWorkSchedule(request)
                .map(this::mapToDomain)
                .compose(ApiErrorMapper.forSingle("Xếp ca làm việc thất bại"));
    }

    @Override
    public Single<List<StaffWorkScheduleResponse>> getStaffWorkSchedules(String date) {
        return scheduleApi.getStaffWorkSchedules(date)
                .compose(ApiErrorMapper.forSingle("Lấy lịch làm việc thất bại"));
    }

    private List<EmployeeWorkSchedule> mapToDomainList(List<EmployeeWorkScheduleResponse> responses) {
        List<EmployeeWorkSchedule> result = new ArrayList<>();
        for (EmployeeWorkScheduleResponse response : responses) {
            result.add(mapToDomain(response));
        }
        return result;
    }

    private EmployeeWorkSchedule mapToDomain(EmployeeWorkScheduleResponse response) {
        EmployeeWorkSchedule.ShiftInfo shiftInfo = null;
        if (response.getShift() != null) {
            shiftInfo = new EmployeeWorkSchedule.ShiftInfo(
                    response.getShift().getShiftId(),
                    response.getShift().getShiftName(),
                    response.getShift().getStartTime(),
                    response.getShift().getEndTime()
            );
        }

        return new EmployeeWorkSchedule(
                response.getEmployeeId(),
                response.getName(),
                response.getPosition(),
                response.getWorkDate(),
                response.getAvatar(),
                response.getStatus(),
                shiftInfo
        );
    }
}
