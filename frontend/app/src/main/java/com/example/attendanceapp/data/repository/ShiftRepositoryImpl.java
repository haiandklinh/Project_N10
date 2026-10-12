package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.shift.CreateShiftRequest;
import com.example.attendanceapp.data.dto.request.shift.UpdateShiftRequest;
import com.example.attendanceapp.data.dto.response.shift.ShiftResponse;
import com.example.attendanceapp.data.local.dao.ShiftDao;
import com.example.attendanceapp.data.local.entity.ShiftEntity;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.ShiftApi;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

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
 * Singleton: nguồn dữ liệu duy nhất cho danh sách ca.
 * Dữ liệu được lưu vĩnh viễn trong Room Database (SQLite).
 */
@Singleton
public class ShiftRepositoryImpl implements ShiftRepository {

    private final ShiftApi shiftApi;
    private final ShiftDao shiftDao;

    @Inject
    public ShiftRepositoryImpl(ShiftApi shiftApi, ShiftDao shiftDao) {
        this.shiftApi = shiftApi;
        this.shiftDao = shiftDao;
    }

    @Override
    public Observable<List<ShiftOption>> observeShifts() {
        return shiftDao.observeAllShifts()
                .toObservable()
                .map(entities -> entities.stream()
                        .map(ShiftEntity::toDomain)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<ShiftOption>> getShifts() {
        return shiftApi.getShifts()
                .map(this::mapToDomainList)
                .flatMap(domainList -> {
                    List<ShiftEntity> entities = domainList.stream()
                            .map(ShiftEntity::fromDomain)
                            .collect(Collectors.toList());
                    // Xóa dữ liệu cũ và chèn danh sách mới nhất từ server
                    return shiftDao.insertShifts(entities)
                            .andThen(Single.just(domainList));
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy danh sách ca làm thất bại"));
    }

    @Override
    public Single<ShiftOption> getShiftDetail(String shiftId) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Single.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        return shiftApi.getShiftDetail(shiftId)
                .map(ShiftResponse::toDomain)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy chi tiết ca làm thất bại"));
    }

    @Override
    public Single<ShiftOption> addShift(String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        if (shiftName == null || shiftName.isEmpty()) {
            return Single.error(new IllegalArgumentException("Tên ca làm không được để trống"));
        }

        CreateShiftRequest request = new CreateShiftRequest(shiftName, startTime, endTime, latitude, longitude);

        return shiftApi.createShift(request)
                .map(ShiftResponse::toDomain)
                .flatMap(created -> shiftDao.insertShift(ShiftEntity.fromDomain(created))
                        .andThen(Single.just(created)))
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Tạo ca làm mới thất bại"));
    }

    @Override
    public Single<ShiftOption> updateShift(String shiftId, String shiftName, String startTime, String endTime, Double latitude, Double longitude) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Single.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        UpdateShiftRequest request = new UpdateShiftRequest(shiftName, startTime, endTime, latitude, longitude);

        return shiftApi.updateShift(shiftId, request)
                .map(response -> {
                    ShiftOption fromServer = response.toDomain();
                    return new ShiftOption(
                            shiftId,
                            (fromServer.getTitle() == null || fromServer.getTitle().isEmpty()) ? shiftName : fromServer.getTitle(),
                            (fromServer.getStartTime() == null || fromServer.getStartTime().isEmpty()) ? startTime : fromServer.getStartTime(),
                            (fromServer.getEndTime() == null || fromServer.getEndTime().isEmpty()) ? endTime : fromServer.getEndTime(),
                            (fromServer.getLatitude() == null) ? latitude : fromServer.getLatitude(),
                            (fromServer.getLongitude() == null) ? longitude : fromServer.getLongitude()
                    );
                })
                .flatMap(updated -> shiftDao.updateShift(ShiftEntity.fromDomain(updated))
                        .andThen(Single.just(updated)))
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Cập nhật ca làm thất bại"));
    }

    @Override
    public Completable deleteShift(String shiftId) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        // 1. Thực hiện xóa API
        return shiftApi.deleteShift(shiftId)
                // Nếu API báo lỗi 404/Không tìm thấy ca (do ca đã xóa trên server trước đó), bỏ qua lỗi và vẫn tiếp tục xóa ở Local DB
                .onErrorResumeNext(throwable -> Completable.complete())
                // 2. Luôn luôn thực thi xóa dưới Room Database cục bộ để làm mới Stream observeShifts
                .andThen(shiftDao.deleteShift(shiftId))
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forCompletable("Xóa ca làm thất bại"));
    }

    private List<ShiftOption> mapToDomainList(List<ShiftResponse> responses) {
        List<ShiftOption> domainList = new ArrayList<>();
        if (responses != null) {
            for (ShiftResponse response : responses) {
                if (response != null) {
                    domainList.add(response.toDomain());
                }
            }
        }
        return domainList;
    }
}