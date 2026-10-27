package com.example.attendanceapp.domain.usecase.schedule;

import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.repository.StaffRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

/**
 * Lịch làm việc của chính nhân viên đang đăng nhập (StaffApi).
 * Server trả toàn bộ lịch, nên lọc theo ngày ở đây. date rỗng/null thì trả tất cả.
 */
public class GetScheduleByDateUseCase {

    private final StaffRepository staffRepository;

    @Inject
    public GetScheduleByDateUseCase(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    /** date định dạng yyyy-MM-dd (giống work_day của server). */
    public Single<List<StaffWorkScheduleResponse>> execute(String date) {
        return staffRepository.getWorkSchedules()
                .map(response -> {
                    List<StaffWorkScheduleResponse> all = response.getData();
                    List<StaffWorkScheduleResponse> result = new ArrayList<>();
                    if (all == null) {
                        return result;
                    }
                    boolean filterByDate = date != null && !date.isEmpty();
                    for (StaffWorkScheduleResponse item : all) {
                        if (item == null) continue;
                        if (!filterByDate || date.equals(item.getWorkDay())) {
                            result.add(item);
                        }
                    }
                    return result;
                });
    }
}