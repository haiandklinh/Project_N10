package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.shift.ShiftItemRequest;
import com.example.attendanceapp.data.dto.response.shift.ShiftItemResponse;
import com.example.attendanceapp.data.local.dao.ShiftDao;
import com.example.attendanceapp.data.local.entity.ShiftEntity;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
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
import retrofit2.HttpException;

/**
 * Singleton: nguồn dữ liệu duy nhất cho danh sách ca.
 * Dữ liệu được lưu trong Room, các ViewModel observe qua observeShifts().
 */
@Singleton
public class ShiftRepositoryImpl implements ShiftRepository {
    private final ShiftApi shiftApi;
    private final ShiftDao shiftDao;
    private final SharedPreferenceApp sharedPreferenceApp;

    @Inject
    public ShiftRepositoryImpl(ShiftApi shiftApi, ShiftDao shiftDao, SharedPreferenceApp sharedPreferenceApp) {
        this.shiftApi = shiftApi;
        this.shiftDao = shiftDao;
        this.sharedPreferenceApp = sharedPreferenceApp;
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
                .map(res -> mapToDomainList(res.getData()))
                .flatMap(domainList -> {
                    List<ShiftEntity> entities = domainList.stream()
                            .map(ShiftEntity::fromDomain)
                            .collect(Collectors.toList());
                    if (sharedPreferenceApp != null && domainList != null) {
                        for (ShiftOption s : domainList) {
                            if (s.getLatitude() != null && s.getLongitude() != null && (s.getLatitude() != 0.0 || s.getLongitude() != 0.0)) {
                                sharedPreferenceApp.saveShiftLocation(s.getTitle(), s.getLatitude(), s.getLongitude(), s.getCheckInRadius() != null ? s.getCheckInRadius() : 100.0);
                                sharedPreferenceApp.saveLatestShiftLocation(s.getLatitude(), s.getLongitude(), s.getCheckInRadius() != null ? s.getCheckInRadius() : 100.0, "");
                            }
                        }
                    }
                    return shiftDao.clearAll()
                            .andThen(shiftDao.insertShifts(entities))
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
                .map(this::toDomain)
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Lấy chi tiết ca làm thất bại"));
    }

    @Override
    public Single<ShiftOption> addShift(String shiftName, String startTime, String endTime,
                                        String allowedCheckInTime, double checkInRadius,
                                        double latitude, double longitude) {
        if (shiftName == null || shiftName.isEmpty()) {
            return Single.error(new IllegalArgumentException("Tên ca làm không được để trống"));
        }

        ShiftItemRequest request = new ShiftItemRequest(
                shiftName, startTime, endTime, allowedCheckInTime, checkInRadius, longitude, latitude);

        return shiftApi.createShift(request)
                .map(this::toDomain)
                .flatMap(created -> shiftDao.insertShift(ShiftEntity.fromDomain(created))
                        .andThen(Single.just(created)))
                .doOnSuccess(created -> {
                    if (sharedPreferenceApp != null) {
                        sharedPreferenceApp.saveShiftLocation(shiftName, latitude, longitude, checkInRadius);
                        sharedPreferenceApp.saveLatestShiftLocation(latitude, longitude, checkInRadius, "");
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Tạo ca làm mới thất bại"));
    }

    @Override
    public Single<ShiftOption> updateShift(String shiftId, String shiftName, String startTime, String endTime,
                                           String allowedCheckInTime, double checkInRadius,
                                           double latitude, double longitude) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Single.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        ShiftItemRequest request = new ShiftItemRequest(
                shiftName, startTime, endTime, allowedCheckInTime, checkInRadius, longitude, latitude);

        return shiftApi.updateShift(shiftId, request)
                .map(response -> {
                    ShiftOption fromServer = toDomain(response);
                    // Server có thể trả thiếu trường: lấy giá trị vừa gửi làm dự phòng.
                    return new ShiftOption(
                            shiftId,
                            isEmpty(fromServer.getTitle()) ? shiftName : fromServer.getTitle(),
                            isEmpty(fromServer.getStartTime()) ? startTime : fromServer.getStartTime(),
                            isEmpty(fromServer.getEndTime()) ? endTime : fromServer.getEndTime(),
                            fromServer.getLatitude() == null ? Double.valueOf(latitude) : fromServer.getLatitude(),
                            fromServer.getLongitude() == null ? Double.valueOf(longitude) : fromServer.getLongitude(),
                            isEmpty(fromServer.getAllowedCheckInTime()) ? allowedCheckInTime : fromServer.getAllowedCheckInTime(),
                            fromServer.getCheckInRadius() == null ? Double.valueOf(checkInRadius) : fromServer.getCheckInRadius()
                    );
                })
                .flatMap(updated -> shiftDao.updateShift(ShiftEntity.fromDomain(updated))
                        .andThen(Single.just(updated)))
                .doOnSuccess(updated -> {
                    if (sharedPreferenceApp != null) {
                        sharedPreferenceApp.saveShiftLocation(shiftName, latitude, longitude, checkInRadius);
                        sharedPreferenceApp.saveLatestShiftLocation(latitude, longitude, checkInRadius, "");
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forSingle("Cập nhật ca làm thất bại"));
    }

    @Override
    public Completable deleteShift(String shiftId) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        return shiftApi.deleteShift(shiftId)
                .ignoreElement()
                // Chỉ bỏ qua 404 (ca đã bị xóa trên server). Lỗi mạng / 500 thì báo lỗi, KHÔNG xóa local.
                .onErrorComplete(ShiftRepositoryImpl::isNotFound)
                // Xóa trong Room để stream observeShifts tự làm mới.
                .andThen(shiftDao.deleteShift(shiftId))
                .subscribeOn(Schedulers.io())
                .compose(ApiErrorMapper.forCompletable("Xóa ca làm thất bại"));
    }

    // --- Helpers ---

    private static boolean isNotFound(Throwable e) {
        return e instanceof HttpException && ((HttpException) e).code() == 404;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private List<ShiftOption> mapToDomainList(List<ShiftItemResponse> responses) {
        List<ShiftOption> domainList = new ArrayList<>();
        if (responses != null) {
            for (ShiftItemResponse response : responses) {
                if (response != null) {
                    domainList.add(toDomain(response));
                }
            }
        }
        return domainList;
    }

    private ShiftOption toDomain(ShiftItemResponse r) {
        double radius = r.getCheckInRadius() > 0 ? r.getCheckInRadius() : 100.0;
        return new ShiftOption(
                r.getId(),
                r.getName(),
                r.getStartTime(),
                r.getEndTime(),
                r.getLatitude(),
                r.getLongitude(),
                r.getAllowedCheckInTime(),
                radius
        );
    }
}
