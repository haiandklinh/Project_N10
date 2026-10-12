package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.work.AssignWorkScheduleRequest;
import com.example.attendanceapp.data.dto.response.schedule.EmployeeWorkScheduleResponse;
import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.dao.WorkScheduleDao;
import com.example.attendanceapp.data.local.entity.WorkScheduleEntity;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.ScheduleApi;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ScheduleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Singleton: nguồn dữ liệu duy nhất cho lịch làm việc.
 * Xếp ca ở bất kỳ đâu cũng cập nhật vào Room, các ViewModel đang observe sẽ tự nhận.
 */
@Singleton
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final ScheduleApi scheduleApi;
    private final WorkScheduleDao workScheduleDao;

    @Inject
    public ScheduleRepositoryImpl(ScheduleApi scheduleApi, WorkScheduleDao workScheduleDao) {
        this.scheduleApi = scheduleApi;
        this.workScheduleDao = workScheduleDao;
    }

    @Override
    public Observable<List<EmployeeWorkSchedule>> observeEmployeeWorkSchedules() {
        return workScheduleDao.observeAllSchedules()
                .toObservable()
                .map(entities -> entities.stream()
                        .map(WorkScheduleEntity::toDomain)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<EmployeeWorkSchedule>> getEmployeeWorkSchedules() {
        return scheduleApi.getEmployeeWorkSchedules()
                .map(this::mapToDomainList)
                .flatMap(domainList -> {
                    List<WorkScheduleEntity> entities = domainList.stream()
                            .map(WorkScheduleEntity::fromDomain)
                            .collect(Collectors.toList());
                    return workScheduleDao.insertOrUpdateSchedules(entities)
                            .andThen(Single.just(domainList));
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách lịch làm việc thất bại"));
    }

    @Override
    public Single<List<EmployeeWorkSchedule>> assignWorkSchedule(List<String> employeeIds, String shiftId, String workDate, String note) {
        AssignWorkScheduleRequest request = new AssignWorkScheduleRequest(
                employeeIds, shiftId, workDate, note
        );
        return scheduleApi.assignWorkSchedule(request)
                .map(this::mapToDomainList)
                .flatMap(domainList -> {
                    List<WorkScheduleEntity> entities = domainList.stream()
                            .map(WorkScheduleEntity::fromDomain)
                            .collect(Collectors.toList());
                    return workScheduleDao.insertOrUpdateSchedules(entities)
                            .andThen(Single.just(domainList));
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Xếp ca làm việc thất bại"));
    }

    @Override
    public Single<List<StaffWorkScheduleResponse>> getStaffWorkSchedules(String date) {
        return scheduleApi.getStaffWorkSchedules(date)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy lịch làm việc thất bại"));
    }

    // --- Helper Methods ---

    private List<EmployeeWorkSchedule> mapToDomainList(List<EmployeeWorkScheduleResponse> responses) {
        List<EmployeeWorkSchedule> result = new ArrayList<>();
        if (responses != null) {
            for (EmployeeWorkScheduleResponse response : responses) {
                if (response != null) {
                    result.add(mapToDomain(response));
                }
            }
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
