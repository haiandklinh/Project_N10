package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.schedule.AssignWorkScheduleRequest;
import com.example.attendanceapp.data.dto.response.schedule.EmployeeWorkScheduleResponse;
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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Singleton: nguồn dữ liệu duy nhất cho lịch làm việc (phía Quản lý).
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
        return scheduleApi.getListStaffWorkSchedule()
                .map(res -> mapToDomainList(res.getData()))
                .flatMap(domainList -> {
                    List<WorkScheduleEntity> entities = domainList.stream()
                            .map(WorkScheduleEntity::fromDomain)
                            .collect(Collectors.toList());
                    return workScheduleDao.clearAll()
                            .andThen(workScheduleDao.insertOrUpdateSchedules(entities))
                            .andThen(Single.just(domainList));
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách lịch làm việc thất bại"));
    }

    @Override
    public Completable assignWorkSchedule(List<String> employeeIds, List<String> shiftIds, String workDate) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Chưa chọn nhân viên"));
        }
        if (shiftIds == null || shiftIds.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Chưa chọn ca làm"));
        }
        if (workDate == null || workDate.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Chưa chọn ngày làm"));
        }

        Completable assignAll = Observable.fromIterable(employeeIds)
                .concatMapCompletable(employeeId ->
                        scheduleApi.assignWorkSchedule(
                                new AssignWorkScheduleRequest(employeeId, workDate, shiftIds)
                        ).ignoreElement());

        return assignAll
                // Nếu lỗi giữa chừng, một số nhân viên đã được xếp: vẫn tải lại để Room khớp server rồi báo lỗi.
                .onErrorResumeNext(e -> refreshQuietly().andThen(Completable.error(e)))
                // Xếp xong mà tải lại lỗi thì không báo thất bại (tránh người dùng bấm xếp lại bị trùng).
                .andThen(Completable.defer(this::refreshQuietly))
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forCompletable("Xếp ca làm việc thất bại"));
    }

    private Completable refreshQuietly() {
        return getEmployeeWorkSchedules().ignoreElement().onErrorComplete();
    }

    // --- Mapping ---

    /**
     * Server trả mỗi nhân viên kèm danh sách ca (shifts). Domain hiện chỉ có 1 ShiftInfo,
     * nên tách thành mỗi ca một dòng. Nhân viên không có ca nào thì giữ 1 dòng với shift = null.
     */
    private List<EmployeeWorkSchedule> mapToDomainList(List<EmployeeWorkScheduleResponse> responses) {
        List<EmployeeWorkSchedule> result = new ArrayList<>();
        if (responses == null) {
            return result;
        }
        for (EmployeeWorkScheduleResponse response : responses) {
            if (response == null) continue;

            List<EmployeeWorkScheduleResponse.ShiftInfo> shifts = response.getShifts();
            if (shifts == null || shifts.isEmpty()) {
                result.add(toDomain(response, null));
                continue;
            }
            for (EmployeeWorkScheduleResponse.ShiftInfo shift : shifts) {
                if (shift != null) {
                    result.add(toDomain(response, shift));
                }
            }
        }
        return result;
    }

    private EmployeeWorkSchedule toDomain(EmployeeWorkScheduleResponse response,
                                          EmployeeWorkScheduleResponse.ShiftInfo shift) {
        EmployeeWorkSchedule.ShiftInfo shiftInfo = null;
        if (shift != null) {
            shiftInfo = new EmployeeWorkSchedule.ShiftInfo(
                    shift.getShiftId(),
                    shift.getShiftName(),
                    shift.getStartTime(),
                    shift.getEndTime()
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
