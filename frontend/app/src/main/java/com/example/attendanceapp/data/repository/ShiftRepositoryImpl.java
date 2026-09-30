package com.example.attendanceapp.data.repository;

import com.example.attendanceapp.data.dto.request.shift.CreateShiftRequest;
import com.example.attendanceapp.data.dto.request.shift.UpdateShiftRequest;
import com.example.attendanceapp.data.dto.response.shift.ShiftResponse;
import com.example.attendanceapp.data.remote.ApiErrorMapper;
import com.example.attendanceapp.data.remote.ShiftApi;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class ShiftRepositoryImpl implements ShiftRepository {

    private final ShiftApi shiftApi;
    // Nếu sau này bạn dùng Room DB để cache ca làm offline, có thể Inject ShiftDao vào đây
    // private final ShiftDao shiftDao;

    @Inject
    public ShiftRepositoryImpl(ShiftApi shiftApi) {
        this.shiftApi = shiftApi;
    }

    @Override
    public Single<List<ShiftOption>> getShifts() {
        return shiftApi.getShifts()
                // 1. Map List<ShiftResponse> (DTO) sang List<ShiftOption> (Domain Model)
                .map(this::mapToDomainList)

                // 2. Tùy chọn: Lưu cache vào Room Database nếu có
                // .flatMap(options -> shiftDao.insertAll(options).toSingleDefault(options))

                // 3. Bắt và chuyển đổi lỗi API
                .compose(ApiErrorMapper.forSingle("Lấy danh sách ca làm thất bại"));
    }

    @Override
    public Single<ShiftOption> getShiftDetail(String shiftId) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Single.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        return shiftApi.getShiftDetail(shiftId)
                .map(ShiftResponse::toDomain)
                .compose(ApiErrorMapper.forSingle("Lấy chi tiết ca làm thất bại"));
    }

    @Override
    public Single<ShiftOption> addShift(String shiftName, String startTime, String endTime, String description) {
        // Validate cơ bản trước khi gọi API
        if (shiftName == null || shiftName.isEmpty()) {
            return Single.error(new IllegalArgumentException("Tên ca làm không được để trống"));
        }

        CreateShiftRequest request = new CreateShiftRequest(shiftName, startTime, endTime, description);

        return shiftApi.createShift(request)
                .map(ShiftResponse::toDomain)
                .compose(ApiErrorMapper.forSingle("Tạo ca làm mới thất bại"));
    }

    @Override
    public Single<ShiftOption> updateShift(String shiftId, String shiftName, String startTime, String endTime, String description) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Single.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        UpdateShiftRequest request = new UpdateShiftRequest(shiftName, startTime, endTime, description);

        return shiftApi.updateShift(shiftId, request)
                .map(ShiftResponse::toDomain)
                .compose(ApiErrorMapper.forSingle("Cập nhật ca làm thất bại"));
    }

    @Override
    public Completable deleteShift(String shiftId) {
        if (shiftId == null || shiftId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("ID ca làm không hợp lệ"));
        }

        return shiftApi.deleteShift(shiftId)
                .compose(ApiErrorMapper.forCompletable("Xóa ca làm thất bại"));
    }

    // --- Helper Methods ---

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